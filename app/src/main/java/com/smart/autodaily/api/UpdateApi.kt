package com.smart.autodaily.api

import com.smart.autodaily.data.entity.AppInfo
import com.smart.autodaily.data.entity.UserInfo
import com.smart.autodaily.data.entity.request.Request
import com.smart.autodaily.data.entity.resp.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface UpdateApi {
    @POST("/getLastUserInfo")
    suspend fun updateUserInfo(@Body request: Request<Int>): Response<UserInfo>

    @GET("/getAppNewVer")
    suspend fun getAppNewVer(): Response<List<AppInfo>>
}