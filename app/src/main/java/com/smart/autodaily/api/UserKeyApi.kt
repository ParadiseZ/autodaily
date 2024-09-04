package com.smart.autodaily.api

import com.smart.autodaily.data.entity.UserInfo
import com.smart.autodaily.data.entity.resp.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface UserKeyApi {
    @GET("/inputKey")
    suspend fun inputKey(@Query("userId") userId :Int, @Query("keyValue") keyValue :String ): Response<UserInfo>

    @GET("/inputInvitorCode")
    suspend fun inputInvitorCode(@Query("userId") userId :Int, @Query("inviteCodeFather") keyValue :String ): Response<UserInfo>
}