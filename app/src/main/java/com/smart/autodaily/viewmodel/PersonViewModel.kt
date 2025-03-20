package com.smart.autodaily.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.smart.autodaily.api.RemoteApi
import com.smart.autodaily.base.BaseViewModel
import com.smart.autodaily.constant.ResponseCode
import com.smart.autodaily.data.appDb
import com.smart.autodaily.data.entity.ServerConfig
import com.smart.autodaily.data.entity.UserInfo
import com.smart.autodaily.data.entity.resp.Response
import com.smart.autodaily.utils.ExceptionUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PersonViewModel(app : Application)  : BaseViewModel(application = app) {
    private val _contact = MutableStateFlow<List<ServerConfig>>(emptyList())
    val contact: StateFlow<List<ServerConfig>> = _contact

    private val _downloadLink = MutableStateFlow<ServerConfig?>(null)
    val downloadLink: StateFlow<ServerConfig?> = _downloadLink

    suspend fun inputKey(userId : Int, key : String):Response<UserInfo>{
        val res = ExceptionUtil.tryCatch(
            tryFun = {
                RemoteApi.userKeyRetrofit.inputKey(userId,key)
            },
            exceptionMsg ="兑换失败!"
        )
        if (res.code==ResponseCode.SUCCESS.code){
            res.data?.let { appViewModel.updateUser(it) }
        }
        return res
    }

    suspend fun inputInvitorCode(userId : Int, key : String) :Response<UserInfo>{
        val res = ExceptionUtil.tryCatch(
            tryFun = {
                RemoteApi.userKeyRetrofit.inputInvitorCode(userId,key)
            },
            exceptionMsg ="保存失败，请稍后重试！"
        )
/*        if (res.code==200){
            res.data?.let { appViewModel.updateUser(it) }
        }*/
        return res
    }

    suspend fun getDownloadLink(){
        if (_downloadLink.value == null){
            runCatching {
                RemoteApi.concatAndNotice.getDownLoadLinkToShare()
            }.onSuccess {
                if (it.code == ResponseCode.SUCCESS.code){
                    _downloadLink.value = it.data
                }
            }
        }
    }

    fun logout(userInfo : UserInfo){
        viewModelScope.launch {
            appDb.userInfoDao.delete(userInfo)
            appViewModel.updateUser(null)
        }
    }

    fun getContact(){
        viewModelScope.launch {
            try {
                if (_contact.value.isEmpty()){
                    RemoteApi.concatAndNotice.getContact().data?.let {
                        _contact.value = it
                    }
                }
            }catch (e : Exception){
                println("获取联系方式异常")
            }
        }
    }
}