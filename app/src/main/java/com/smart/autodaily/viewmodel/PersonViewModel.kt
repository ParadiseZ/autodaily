package com.smart.autodaily.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.smart.autodaily.api.RemoteApi
import com.smart.autodaily.base.BaseViewModel
import com.smart.autodaily.constant.ResponseCode
import com.smart.autodaily.data.appDb
import com.smart.autodaily.data.entity.UserInfo
import com.smart.autodaily.data.entity.resp.Response
import com.smart.autodaily.utils.ExceptionUtil
import kotlinx.coroutines.launch

class PersonViewModel(app : Application)  : BaseViewModel(application = app) {
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

    fun logout(userInfo : UserInfo){
        viewModelScope.launch {
            appDb.userInfoDao.delete(userInfo)
            appViewModel.updateUser(null)
        }
    }
}