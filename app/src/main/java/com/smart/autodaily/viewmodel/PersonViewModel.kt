package com.smart.autodaily.viewmodel

import android.app.Application
import com.smart.autodaily.api.RemoteApi
import com.smart.autodaily.base.BaseViewModel
import com.smart.autodaily.data.appDb
import com.smart.autodaily.data.entity.UserInfo
import com.smart.autodaily.data.entity.resp.Response
import com.smart.autodaily.utils.ExceptionUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PersonViewModel(app : Application)  : BaseViewModel(application = app) {
    suspend fun getUserInfoLocal() : UserInfo?{
        val user = withContext(Dispatchers.IO){
            appDb!!.userInfoDao.queryUserInfo()
        }
        return user
    }

    suspend fun inputKey(userId : Int, key : String):Response<UserInfo>{
        return ExceptionUtil.tryCatch(
            tryFun = {
                RemoteApi.userKeyRetrofit.inputKey(userId,key)
            },
            exceptionMsg ="兑换失败"
        )
    }

    suspend fun logout(userInfo : UserInfo){
        withContext(Dispatchers.IO){
            appDb!!.userInfoDao.delete(userInfo)
        }
    }
}