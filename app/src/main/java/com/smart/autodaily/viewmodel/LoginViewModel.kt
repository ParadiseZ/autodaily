package com.smart.autodaily.viewmodel

import android.app.Application
import com.smart.autodaily.api.RemoteApi
import com.smart.autodaily.base.BaseViewModel
import com.smart.autodaily.data.appDb
import com.smart.autodaily.data.entity.LoginResponse
import com.smart.autodaily.data.entity.request.LoginByEmailRequest
import com.smart.autodaily.data.entity.resp.Response
import com.smart.autodaily.utils.EncryptUtil
import com.smart.autodaily.utils.TokenManager
import java.io.IOException


class LoginViewModel(app: Application): BaseViewModel(app) {
    suspend fun loginByEmail(email: String, password: String) : Response<LoginResponse> {
        try {
            // 对密码进行SHA-256加密
            val encryptedPassword = EncryptUtil.encryptSHA256(password)
            val loginResult = RemoteApi.registerLoginRetrofit.loginByEmail(LoginByEmailRequest(email, encryptedPassword))
            if (loginResult.code == 200) {
                loginResult.data?.let {
                    it.userInfo.isLogin = true
                    appDb.userInfoDao.insert(it.userInfo)
                    appViewModel.updateUser(it.userInfo)
                    TokenManager.saveToken(it.token)
                }
            }
            return loginResult
        }catch (e: IOException){
            return Response.error("网络异常，登录失败:${e.message}")
        }catch (e: Exception){
            return Response.error("未知异常，登录失败：${e.message}")
        }
    }
}