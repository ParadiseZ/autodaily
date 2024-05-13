package com.smart.autodaily.viewmodel

import android.app.Application
import com.smart.autodaily.api.RemoteApi
import com.smart.autodaily.base.BaseViewModel
import com.smart.autodaily.constant.LoginResult
import com.smart.autodaily.data.appDb
import com.smart.autodaily.data.entity.LoginByEmailRequest
import java.io.IOException


class LoginViewModel(app: Application): BaseViewModel(app) {
    suspend fun loginByEmail(email: String, password: String) : LoginResult {
        try {
            val loginResult = RemoteApi.registerLoginRetrofit.loginByEmail(LoginByEmailRequest(email, password))
            if (loginResult.code == 200) {
                loginResult.data?.let {
                    appDb!!.userInfoDao.insert(it)
                }
            }
            return LoginResult.LOGIN_SUCCESS
        }catch (e: IOException){
            return LoginResult.NETWORK_ERROR
        }catch (e: Exception){
            return LoginResult.UNKNOWN_ERROR
        }
    }
}