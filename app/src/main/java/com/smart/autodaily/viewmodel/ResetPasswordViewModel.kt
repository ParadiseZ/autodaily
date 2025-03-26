package com.smart.autodaily.viewmodel

import android.app.Application
import com.smart.autodaily.api.RemoteApi
import com.smart.autodaily.base.BaseViewModel
import com.smart.autodaily.data.entity.request.RestPwdByEmailRequest
import com.smart.autodaily.data.entity.resp.Response
import com.smart.autodaily.utils.EncryptUtil
import java.io.IOException

class ResetPasswordViewModel(app: Application): BaseViewModel(app) {
    suspend fun resetPwdByEmail(email: String, code: String, pwd: String) : Response<String>{
        return try {
            val encryptedPassword = EncryptUtil.encryptSHA256(pwd)
            RemoteApi.registerLoginRetrofit.resetPwdByEmail( RestPwdByEmailRequest(email, code, encryptedPassword) )
        }catch (e:IOException){
            Response.error(201,"发送失败，网络异常")
        }catch (e : Exception){
            Response.error(201,"发送失败，未知异常，请稍后重试")
        }
    }
    suspend fun sendEmailCode(email: String, msgType: Short):Response<String> {
        return try {
            RemoteApi.registerLoginRetrofit.sendEmailCode(email, msgType)
        }catch (e:IOException){
            Response.error(201,"发送失败，网络异常")
        }catch (e : Exception){
            Response.error(201,"发送失败，未知异常，请稍后重试")
        }
    }
}