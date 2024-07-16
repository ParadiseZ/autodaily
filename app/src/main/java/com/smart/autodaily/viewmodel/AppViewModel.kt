package com.smart.autodaily.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smart.autodaily.api.RemoteApi
import com.smart.autodaily.data.appDb
import com.smart.autodaily.data.entity.ScriptInfo
import com.smart.autodaily.data.entity.UserInfo
import com.smart.autodaily.data.entity.request.Request
import com.smart.autodaily.handler.RunScript
import com.smart.autodaily.utils.ToastUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppViewModel (application: Application) : AndroidViewModel(application){
    /*private val _localScriptList = MutableStateFlow<PagingData<ScriptInfo>>(PagingData.empty())
    val localScriptList: StateFlow<PagingData<ScriptInfo>> = _localScriptList*/
    //本地脚本列表
    private val _localScriptAll = MutableStateFlow<List<ScriptInfo>>(emptyList())
    val localScriptListAll: StateFlow<List<ScriptInfo>> = _localScriptAll
    //加载本地数据的标志
    private val _loadDataFlagFlow = MutableStateFlow(false)
    val loadDataFlagFlow: StateFlow<Boolean> get() = _loadDataFlagFlow
    //本地用户
    private val _user  = MutableStateFlow<UserInfo?>(null)
    val user : StateFlow<UserInfo?> get() = _user

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
                        if (res.code == 200){
                            res.data?.let { it1 -> appDb?.userInfoDao?.update(it1) }
                            loadUserInfo()
                        }else if (res.code== 999){
                            ToastUtil.showLong(getApplication<Application>().applicationContext, res.message.toString())
                        }
                    }
                }catch (e : Exception){
                    e.message
                }
            }
        }
    }

    private suspend fun loadUserInfo() {
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

    fun runScript(){
        //RunScript.runScript()
    }

    fun stopRunScript(){
        RunScript.stopRunScript()
    }
}