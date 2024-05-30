package com.smart.autodaily.api

import com.smart.autodaily.data.entity.request.Request
import com.smart.autodaily.data.entity.resp.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface RunApi {
    @POST("/runCheck")
    suspend fun runCheck(@Body request: Request<List<Int>>): Response<List<Int>>
}