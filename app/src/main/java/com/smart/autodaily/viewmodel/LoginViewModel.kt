package com.smart.autodaily.viewmodel

import android.app.Application
import com.smart.autodaily.api.RemoteApi
import com.smart.autodaily.base.BaseViewModel
import com.smart.autodaily.data.appDb
import com.smart.autodaily.data.entity.UserInfo
import com.smart.autodaily.data.entity.request.LoginByEmailRequest
import com.smart.autodaily.data.entity.resp.Response
import java.io.IOException


class LoginViewModel(app: Application): BaseViewModel(app) {
    suspend fun loginByEmail(email: String, password: String) : Response<UserInfo> {
        try {
            val loginResult = RemoteApi.registerLoginRetrofit.loginByEmail(LoginByEmailRequest(email, password))
            if (loginResult.code == 200) {
                loginResult.data?.let {
                    it.isLogin=true
                    appDb.userInfoDao.insert(it)
                    appViewModel.updateUser(it)
                }
            }
            return loginResult
        }catch (e: IOException){
            return Response.error("网络异常，登录失败")
        }catch (e: Exception){
            println(e.message)
            return Response.error("未知异常，登录失败")
        }
    }
}