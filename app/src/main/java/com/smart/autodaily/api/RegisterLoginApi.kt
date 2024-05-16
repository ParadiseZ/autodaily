package com.smart.autodaily.api

import com.smart.autodaily.data.entity.LoginByEmailRequest
import com.smart.autodaily.data.entity.RegisterByEmailRequest
import com.smart.autodaily.data.entity.RestPwdByEmailRequest
import com.smart.autodaily.data.entity.UserInfo
import com.smart.autodaily.data.entity.resp.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query


interface RegisterLoginApi {
    @POST("/loginByEmail")
    suspend fun loginByEmail(@Body request: LoginByEmailRequest): Response<UserInfo>

    @POST("/registerByEmail")
    suspend fun registerByEmail(@Body request: RegisterByEmailRequest): Response<String>

    @GET("/sendEmailCode")
    suspend fun sendEmailCode(@Query("email") email: String,@Query("msgType") msgType: Short):Response<String>

    @POST("/resetPwdByEmail")
    suspend fun resetPwdByEmail(@Body request: RestPwdByEmailRequest):Response<String>
}