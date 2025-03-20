package com.smart.autodaily.api

import com.smart.autodaily.data.entity.ServerConfig
import com.smart.autodaily.data.entity.resp.Response
import retrofit2.http.GET

interface ConcatAndNotice {
    @GET("/getContact")
    suspend fun getContact() : Response<List<ServerConfig>>

    @GET("/getDownLoadLinkToShare")
    suspend fun getDownLoadLinkToShare() : Response<ServerConfig>
}