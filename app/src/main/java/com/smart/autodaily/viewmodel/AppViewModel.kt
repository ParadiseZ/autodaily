package com.smart.autodaily.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smart.autodaily.api.RemoteApi
import com.smart.autodaily.constant.MODEL_BIN
import com.smart.autodaily.constant.MODEL_PARAM
import com.smart.autodaily.constant.ResponseCode
import com.smart.autodaily.data.appDb
import com.smart.autodaily.data.entity.DownloadState
import com.smart.autodaily.data.entity.ScriptActionInfo
import com.smart.autodaily.data.entity.ScriptInfo
import com.smart.autodaily.data.entity.UserInfo
import com.smart.autodaily.data.entity.request.Request
import com.smart.autodaily.data.entity.resp.Response
import com.smart.autodaily.feature.scripting.domain.state.ScriptExecutionState
import com.smart.autodaily.feature.scripting.domain.usecase.*
import com.smart.autodaily.handler.ERROR
// import com.smart.autodaily.handler.isRunning // Replaced by GetOverallScriptingStateUseCase
import com.smart.autodaily.utils.DownloadManager
import com.smart.autodaily.utils.Lom
import com.smart.autodaily.utils.SnackbarUtil
import com.smart.autodaily.utils.cancelChildrenJob
import com.smart.autodaily.utils.deleteFile
import com.smart.autodaily.utils.logScope
import com.smart.autodaily.utils.runScope
import com.smart.autodaily.utils.updateScriptSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDateTime
import splitties.init.appCtx
import java.io.File

val workType by lazy {
    mutableStateOf("")
}

class AppViewModel (
    application: Application,
    // Assuming these use cases are provided via DI (e.g., Hilt) or a ViewModel factory.
    // For this refactor, direct default instantiation is not shown as it depends on service/repo availability.
    private val startScriptUseCase: StartScriptUseCase,
    private val stopScriptUseCase: StopScriptUseCase,
    private val getScriptExecutionStateUseCase: GetScriptExecutionStateUseCase,
    private val getOverallScriptingStateUseCase: GetOverallScriptingStateUseCase,
    private val initializeGlobalScriptConfigUseCase: InitializeGlobalScriptConfigUseCase,
    private val deleteOldRunStatusUseCase: DeleteOldRunStatusUseCase
) : AndroidViewModel(application){

    // Expose overall scripting state
    val isAnyScriptRunning: StateFlow<Boolean> = getOverallScriptingStateUseCase.execute()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Keep track of the primary script being managed by UI, if any
    private val _currentManagedScriptId = MutableStateFlow<Int?>(null)
    val currentManagedScriptId: StateFlow<Int?> = _currentManagedScriptId

    // Expose state for a specific script (e.g., the one started via UI)
    // This can be collected by UI when currentManagedScriptId is not null.
    fun getManagedScriptState(): StateFlow<ScriptExecutionState?> {
        return currentManagedScriptId.value?.let { scriptId ->
            getScriptExecutionStateUseCase.execute(scriptId)
        } ?: MutableStateFlow(null) // Return a flow with null if no script is managed
    }


    //本地脚本列表
    private val _localScriptAll = MutableStateFlow<List<ScriptInfo>>(emptyList())
    val localScriptListAll: StateFlow<List<ScriptInfo>> = _localScriptAll
    //本地用户
    private val _user = MutableStateFlow<UserInfo?>(null)
    val user : StateFlow<UserInfo?> get() = _user

    init {
        viewModelScope.launch {
            // Perform initial setup tasks that were in App.kt
            initializeAppDefaults()
            updateAndLoadUserInfo()
            loadScriptAll()
        }
    }

    private suspend fun initializeAppDefaults() {
        initializeGlobalScriptConfigUseCase.execute()
        deleteOldRunStatusUseCase.execute()
    }

    private fun updateAndLoadUserInfo() {
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                loadUserInfo()
                try {
                    if(_user.value!=null){
                        val req : Request<Int> = Request(data = _user.value!!.userId)
                        val res = RemoteApi.updateRetrofit.updateUserInfo(req)
                        if (res.code == ResponseCode.SUCCESS.code){
                            res.data?.let { it1 -> appDb.userInfoDao.update(it1) }
                            loadUserInfo()
                        }else{
                            SnackbarUtil.show(res.message.toString())
                        }
                    }
                }catch (e : Exception){
                    e.message
                }
            }
        }
    }

    suspend fun loadUserInfo() {
        appDb.userInfoDao.queryUserInfo().collectLatest {
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
                appDb.scriptInfoDao.getLocalScriptAll().collectLatest {
                    _localScriptAll.value = it
                }
            }
        }
    }

    //更新本地数据
    fun updateScript(scriptInfo: ScriptInfo){
        viewModelScope.launch {
            appDb.scriptInfoDao.update(scriptInfo)
            /*withContext(Dispatchers.IO){
                appDb.scriptInfoDao.update(scriptInfo)
            }*/
            //RunScript.updateScript(scriptInfo)
        }
    }

    // Removed setIsRunning as state is now driven by ScriptStateManager via use cases

    fun startScript(script: ScriptInfo) {
        viewModelScope.launch {
            // Potentially set this script as the "managed" one by UI
            _currentManagedScriptId.value = script.scriptId
            // Actual start logic
            startScriptUseCase.execute(script)
        }
    }

    fun stopCurrentScript() { // Renamed for clarity, stops the "managed" script
        viewModelScope.launch {
            _currentManagedScriptId.value?.let { scriptId ->
                stopScriptUseCase.execute(scriptId)
                // Optionally clear currentManagedScriptId or wait for state to become Idle/Finished
            } ?: Lom.w("AppViewModel", "stopCurrentScript called but no currentManagedScriptId set.")
            // Old cancellation of runScope/logScope might not be needed if they were specific to RunScript
            // runScope.coroutineContext.cancelChildren()
            // logScope.coroutineContext.cancelChildren()
        }
    }

    // If a generic "stop any script by ID" is needed from elsewhere:
    fun stopScriptById(scriptId: Int) {
        viewModelScope.launch {
            stopScriptUseCase.execute(scriptId)
            if (_currentManagedScriptId.value == scriptId) {
                // If the stopped script was the one managed by UI, update UI state if necessary
            }
        }
    }


    //下载
    suspend fun downScriptByScriptId(scriptInfo : ScriptInfo) : Boolean {
        runCatching {
            if(scriptInfo.scriptId!=0){
                downloadModel(scriptInfo)
            }
            downByScriptId(scriptInfo)
            return true
        }.onFailure {
            return false
        }
        return true
    }

    //下载模型
    private suspend fun downloadModel(scriptInfo : ScriptInfo) {
        val modelFilePath = File(appCtx.getExternalFilesDir(""), scriptInfo.modelPath)
        val externalParamFile = File(modelFilePath, MODEL_PARAM)
        //删除旧文件
        deleteFile(externalParamFile)
        if (!modelFilePath.exists()){
            modelFilePath.mkdirs()
        }
        DownloadManager.download(scriptInfo.scriptId, externalParamFile,"param").collect{
            when (it) {
                is DownloadState.InProgress -> {
                }
                is DownloadState.Success -> {
                }
                is DownloadState.Error -> {
                    deleteFile(externalParamFile)
                    Lom.n(ERROR, "下载${scriptInfo.scriptName}模型参数失败")
                    return@collect
                }
            }
        }
        val externalBinFile = File(modelFilePath , MODEL_BIN)
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
                    Lom.n(ERROR, "下载${scriptInfo.scriptName}模型文件失败")
                    scriptInfo.process.intValue = -1
                }
            }
        }
    }

    //下载脚本数据
    private suspend fun downByScriptId(scriptInfo: ScriptInfo) {
        val siNew = RemoteApi.searchDownRetrofit.dlScriptInfo(scriptInfo.scriptId)
        val result = RemoteApi.searchDownRetrofit.downScriptSetByScriptId(scriptInfo.scriptId)
        var scriptAction = Response<List<ScriptActionInfo>>()
        //var globalScriptSetResult = Response<List<ScriptSetInfo>>()
        var setIds : List<Int> = emptyList()
        if(scriptInfo.scriptId !=0){
            scriptAction = RemoteApi.searchDownRetrofit.downloadActionInfoByScriptId(scriptInfo.scriptId)
            val localScriptSetGlobal = appDb.scriptSetInfoDao.countScriptSetByScriptId(0)
            if (localScriptSetGlobal == 0) {
                //globalScriptSetResult = RemoteApi.searchDownRetrofit.downScriptSetByScriptId(0)
                val siGlobal = RemoteApi.searchDownRetrofit.dlScriptInfo(0)
                val setGlobal = RemoteApi.searchDownRetrofit.downScriptSetByScriptId(0)
                appDb.runInTransaction{
                    siGlobal.data?.let { si->
                        //scriptInfo
                        si.isDownloaded = 1
                        si.lastVersion?.let {
                            si.scriptVersion = it   //更新（即重新下载）时使用
                        }
                        si.lastVersion = null //检测更新时使用
                        si.downloadTime = LocalDateTime.now().toString()
                        appDb.scriptInfoDao.insert(si)
                    }
                    setGlobal.data?.let { setList->
                        updateScriptSet(setList)
                        setIds = setList.map { it.setId }
                        appDb.scriptSetInfoDao.deleteScriptSetByIds(0,setIds)
                    }
                }
            }
        }

        //val scriptSetDownload = RemoteApi.searchDownRetrofit.downScriptSetByScriptId(scriptId)
        appDb.runInTransaction{
            siNew.data?.let { si->
                //scriptInfo
                si.isDownloaded = 1
                si.lastVersion?.let {
                    si.scriptVersion = it   //更新（即重新下载）时使用
                }
                si.checkedFlag = scriptInfo.checkedFlag
                si.lastVersion = null //检测更新时使用
                si.downloadTime = LocalDateTime.now().toString()
                appDb.scriptInfoDao.insert(si)
            }
            if (scriptInfo.scriptId!=0){
                //ScriptSet 全局设置
                /*globalScriptSetResult.data?.let {
                    updateScriptSet(it)
                    //appDb.scriptSetInfoDao.insert(it)
                }*/
                //actionInfo 动作信息
                scriptAction.data?.let {
                    appDb.scriptActionInfoDao.insert(it)
                    //fts表
                }
                scriptInfo.process.intValue = -1
            }
            //ScriptSet 设置
            result.data?.let { setList->
                updateScriptSet(setList)
                setIds = setList.map { it.setId }
                appDb.scriptSetInfoDao.deleteScriptSetByIds(setList[0].scriptId,setIds)
            }
        }
    }

    fun getScriptInfoGlobal(): ScriptInfo?{
        return appDb.scriptInfoDao.getScriptInfoByScriptId(0)
    }
    override fun onCleared() {
        super.onCleared()
        cancelChildrenJob()
    }
}