package com.smart.autodaily.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smart.autodaily.api.RemoteApi
import com.smart.autodaily.constant.MODEL_BIN
import com.smart.autodaily.constant.MODEL_PARAM
import com.smart.autodaily.constant.ResponseCode
import com.smart.autodaily.constant.WORK_TYPE01
import com.smart.autodaily.constant.WORK_TYPE02
import com.smart.autodaily.constant.WORK_TYPE03
import com.smart.autodaily.data.appDb
import com.smart.autodaily.data.entity.DownloadState
import com.smart.autodaily.data.entity.ScriptInfo
import com.smart.autodaily.data.entity.ScriptSetInfo
import com.smart.autodaily.data.entity.UserInfo
import com.smart.autodaily.data.entity.request.Request
import com.smart.autodaily.data.entity.resp.Response
import com.smart.autodaily.handler.RunScript
import com.smart.autodaily.utils.DownloadManager
import com.smart.autodaily.utils.ServiceUtil
import com.smart.autodaily.utils.ShizukuUtil
import com.smart.autodaily.utils.deleteFile
import com.smart.autodaily.utils.toastOnUi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDateTime
import splitties.init.appCtx
import java.io.File

class AppViewModel (application: Application) : AndroidViewModel(application){
    //本地脚本列表
    private val _localScriptAll = MutableStateFlow<List<ScriptInfo>>(emptyList())
    val localScriptListAll: StateFlow<List<ScriptInfo>> = _localScriptAll
    //加载本地数据的标志
    private val _loadDataFlagFlow = MutableStateFlow(false)
    val loadDataFlagFlow: StateFlow<Boolean> get() = _loadDataFlagFlow
    //本地用户
    private val _user  = MutableStateFlow<UserInfo?>(null)
    val user : StateFlow<UserInfo?> get() = _user

    private val _isRunning = MutableStateFlow(0)
    val isRunning : StateFlow<Int> get() = _isRunning

    private val _supervisorJob = SupervisorJob()
    val appScope = CoroutineScope(Dispatchers.IO + _supervisorJob)

    //加载用户
    init {
        viewModelScope.launch {
            updateAndLoadUserInfo()
            loadScriptAll()
        }
    }


    private fun updateAndLoadUserInfo(){
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                loadUserInfo()
                try {
                    if(_user.value!=null){
                        val req : Request<Int> = Request(data = _user.value!!.userId)
                        val res = RemoteApi.updateRetrofit.updateUserInfo(req)
                        if (res.code == ResponseCode.SUCCESS.code){
                            res.data?.let { it1 -> appDb?.userInfoDao?.update(it1) }
                            loadUserInfo()
                        }else{
                            appCtx.toastOnUi(res.message.toString())
                        }
                    }
                }catch (e : Exception){
                    e.message
                }
            }
        }
    }

    suspend fun loadUserInfo() {
        appDb?.userInfoDao?.queryUserInfo()?.collectLatest {
            _user.value = it
        }
    }
    fun updateUser(userInfo: UserInfo?){
        _user.value = userInfo
    }
    //加载本地所有数据
    private fun loadScriptAll(){
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                appDb!!.scriptInfoDao.getLocalScriptAll().collectLatest {
                    _localScriptAll.value = it
                }
            }

        }
    }

    //更新本地数据
    fun updateScript(scriptInfo: ScriptInfo){
        viewModelScope.launch {
            appDb!!.scriptInfoDao.update(scriptInfo)
            /*withContext(Dispatchers.IO){
                appDb!!.scriptInfoDao.update(scriptInfo)
            }*/
            //RunScript.updateScript(scriptInfo)
        }
    }

    suspend fun runScript(){
        RunScript.globalSetMap.value[8]?.let {
            when(it.setValue) {
                WORK_TYPE01 -> {
                    _isRunning.value = 1
                }
                WORK_TYPE02 -> {
                    _isRunning.value = 2//启动服务
                    RunScript.initScriptData(appDb!!.scriptInfoDao.getAllScriptByChecked())
                    ServiceUtil.runUserService(appCtx)
                    ServiceUtil.waitShizukuService()
                    if(ShizukuUtil.grant && ShizukuUtil.iUserService != null){
                        _isRunning.value = 1//运行中
                        RunScript.runScriptByAdb()
                    }
                    //(manActivityCtx as MainActivity).requestOverlayPermission()
                }
                WORK_TYPE03 -> {
                    _isRunning.value = 1
                }
            }
        }
    }

    fun stopRunScript(){
        _isRunning.value = 0
        appScope.coroutineContext.cancelChildren()
        _supervisorJob.cancelChildren()
    }

    //下载
    fun downScriptByScriptId(scriptInfo : ScriptInfo) {
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                downloadModel(scriptInfo)
                downByScriptId(scriptInfo)
            }
        }
    }

    //下载模型
    private suspend fun downloadModel(scriptInfo : ScriptInfo){
        val externalParamFile = File(appCtx.getExternalFilesDir("") , MODEL_PARAM)
        //删除旧文件
        deleteFile(externalParamFile)
        DownloadManager.download(scriptInfo.scriptId, externalParamFile,"param").collect{
            when (it) {
                is DownloadState.InProgress -> {
                }
                is DownloadState.Success -> {
                }
                is DownloadState.Error -> {
                    deleteFile(externalParamFile)
                    return@collect
                }
            }
        }
        val externalBinFile = File(appCtx.getExternalFilesDir("") , MODEL_BIN)
        //删除旧文件
        deleteFile(externalBinFile)
        DownloadManager.download(scriptInfo.scriptId, externalBinFile,"bin").collect{
            when (it) {
                is DownloadState.InProgress -> {
                    //进度条设置
                    scriptInfo.process.intValue = it.progress
                }
                is DownloadState.Success -> {
                }
                is DownloadState.Error -> {
                    deleteFile(externalBinFile)
                    scriptInfo.process.intValue = -1
                }
            }
        }
    }

    //下载脚本数据
    private suspend fun downByScriptId(scriptInfo: ScriptInfo) {
        val result = RemoteApi.searchDownRetrofit.downScriptSetByScriptId(scriptInfo.scriptId)
        //val actionInfo = RemoteApi.searchDownRetrofit.downloadActionInfoByScriptId(scriptInfo.scriptId)
        var globalScriptSetResult = Response<List<ScriptSetInfo>>()
        val localScriptSetGlobal = appDb?.scriptSetInfoDao?.countScriptSetByScriptId(0)
        if (localScriptSetGlobal == 0) {
            globalScriptSetResult = RemoteApi.searchDownRetrofit.downScriptSetByScriptId(0)
        }
        //val scriptSetDownload = RemoteApi.searchDownRetrofit.downScriptSetByScriptId(scriptId)
        appDb?.runInTransaction{
            //scriptInfo
            scriptInfo.isDownloaded = 1
            scriptInfo.scriptVersion = scriptInfo.lastVersion!!
            scriptInfo.lastVersion = null //检测更新时使用
            scriptInfo.downloadTime = LocalDateTime.now().toString()
            appDb?.scriptInfoDao?.insert(scriptInfo)
            //ScriptSet 全局设置
            globalScriptSetResult.data?.let {
                appDb?.scriptSetInfoDao?.insert(it)
            }
            //scriptSetDownload.data?.let {
            //appDb?.scriptSetInfoDao?.insert(it)
            // }
            //ScriptSet 设置
            result.data?.let {
                /*it.map{ ssi->

                }*/
                appDb?.scriptSetInfoDao?.insert(it)
            }
            //picInfo 图片信息
            //actionInfo 动作信息
            /*actionInfo.data?.let {
                appDb?.scriptActionInfoDao?.insert(it)
            }*/
            scriptInfo.process.intValue = -1
        }
    }

    override fun onCleared() {
        super.onCleared()
        appScope.cancel()
        _supervisorJob.cancel()
    }
}