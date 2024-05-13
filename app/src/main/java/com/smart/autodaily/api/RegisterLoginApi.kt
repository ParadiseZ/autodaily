package com.smart.autodaily.api

import com.smart.autodaily.data.entity.LoginByEmailRequest
import com.smart.autodaily.data.entity.RegisterByEmailRequest
import com.smart.autodaily.data.entity.UserInfo
import com.smart.autodaily.data.entity.resp.Response
import retrofit2.http.Body
import retrofit2.http.POST


interface RegisterLoginApi {
    @POST("loginByEmail")
    suspend fun loginByEmail(@Body request: LoginByEmailRequest): Response<UserInfo>

    @POST("registerByEmail")
    suspend fun registerByEmail(@Body request: RegisterByEmailRequest): Response<UserInfo>
}