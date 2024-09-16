package com.smart.autodaily.api

import com.smart.autodaily.data.entity.KeyTypeExchange
import com.smart.autodaily.data.entity.resp.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface KeyTypeApi {
    @GET("/getExchangeVipKeyType")
    suspend fun getKeyTypeList(@Query("page") page : Int, @Query("pageSize") pageSize : Int) : Response<List<KeyTypeExchange>>
}