package com.smart.autodaily.api

import com.smart.autodaily.data.entity.UserInfo
import com.smart.autodaily.data.entity.resp.Response
import retrofit2.http.Field
import retrofit2.http.POST

interface UserKeyApi {
    @POST("/inputKey")
    suspend fun inputKey(@Field("userId") userId :Int, @Field("keyValue") keyValue :String ): Response<UserInfo>
}