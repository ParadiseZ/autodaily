package com.smart.autodaily.viewmodel

import android.app.Application
import com.smart.autodaily.api.RemoteApi
import com.smart.autodaily.base.BaseViewModel
import com.smart.autodaily.data.entity.request.RegisterByEmailRequest
import com.smart.autodaily.data.entity.resp.Response
import com.smart.autodaily.utils.EncryptUtil
import java.io.IOException

class RegisterViewModel(app: Application): BaseViewModel(app) {

    suspend fun registerByEmail(username: String,emailCheckCode : String, password: String, inviteCodeFather: String): Response<String> {
        return try {
            val encryptedPassword = EncryptUtil.encryptSHA256(password)
            RemoteApi.registerLoginRetrofit.registerByEmail(RegisterByEmailRequest(username, emailCheckCode, encryptedPassword, inviteCodeFather))
        }catch (_: IOException) {
            Response.error("网络异常，注册失败")
        }catch (_: Exception){
            Response.error("未知异常，注册失败")
        }
    }

    suspend fun sendEmailCode(email: String,msgType : Short):Response<String> {
        return try {
            RemoteApi.registerLoginRetrofit.sendEmailCode(email, msgType)
        }catch (_:IOException){
            Response.error(201,"发送失败，网络异常")
        }catch (_ : Exception){
            Response.error(201,"发送失败，未知异常，请稍后重试")
        }
    }
}