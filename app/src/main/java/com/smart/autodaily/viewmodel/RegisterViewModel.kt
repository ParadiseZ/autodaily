package com.smart.autodaily.viewmodel

import android.app.Application
import com.smart.autodaily.api.RemoteApi
import com.smart.autodaily.base.BaseViewModel
import com.smart.autodaily.constant.RegisterResult
import com.smart.autodaily.data.entity.RegisterByEmailRequest
import java.io.IOException

class RegisterViewModel(app: Application): BaseViewModel(app) {

    suspend fun registerByEmail(username: String,emailCheckCode : String, password: String, inviteCodeFather: String): RegisterResult {
        try {
            val registerResult = RemoteApi.registerLoginRetrofit.registerByEmail(RegisterByEmailRequest(username, emailCheckCode, password, inviteCodeFather))
            if (registerResult.code == 200) {
                return RegisterResult.REGISTER_SUCCESS
            } else {
                return RegisterResult.EMAIL_REGISTERED
            }
        }catch (e: IOException) {
            return RegisterResult.NETWORK_ERROR
        }catch (e: Exception){
            return RegisterResult.UNKNOWN_ERROR
        }
    }

    suspend fun sendEmailCode(email: String){
        RemoteApi.registerLoginRetrofit.sendEmailCode(email)
    }
}