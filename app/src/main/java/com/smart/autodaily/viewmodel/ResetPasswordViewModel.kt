package com.smart.autodaily.viewmodel

import android.app.Application
import com.smart.autodaily.api.RemoteApi
import com.smart.autodaily.base.BaseViewModel
import com.smart.autodaily.data.entity.RestPwdByEmailRequest
import com.smart.autodaily.data.entity.resp.Response
import java.io.IOException

class ResetPasswordViewModel(app: Application): BaseViewModel(app) {
    suspend fun resetPwdByEmail(email: String, code: String, pwd: String) : Response<String>{
        try {
            return RemoteApi.registerLoginRetrofit.resetPwdByEmail( RestPwdByEmailRequest(email, code, pwd) )
        }catch (e:IOException){
            return Response.error(201,"发送失败，网络异常")
        }catch (e : Exception){
            return Response.error(201,"发送失败，未知异常，请稍后重试")
        }
    }
    suspend fun sendEmailCode(email: String):Response<String> {
        try {
            return RemoteApi.registerLoginRetrofit.sendEmailCode(email)
        }catch (e:IOException){
            return Response.error(201,"发送失败，网络异常")
        }catch (e : Exception){
            return Response.error(201,"发送失败，未知异常，请稍后重试")
        }
    }
}