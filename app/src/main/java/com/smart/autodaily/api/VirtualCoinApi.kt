package com.smart.autodaily.api

import com.smart.autodaily.data.entity.UserInfo
import com.smart.autodaily.data.entity.VirtualCoinRecord
import com.smart.autodaily.data.entity.resp.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface VirtualCoinApi {
    @GET("/getCoinRecord")
    suspend fun getCoinRecord(@Query("userId")userId : Int,@Query("page") page : Int, @Query("pageSize") pageSize : Int) : Response<List<VirtualCoinRecord>>

    @GET("/exchangeVip")
    suspend fun exchangeVip(@Query("userId")userId : Int, @Query("keyTypeId") keyTypeId : Int) : Response<UserInfo>
}