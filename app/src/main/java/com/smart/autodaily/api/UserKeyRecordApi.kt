package com.smart.autodaily.api

import com.smart.autodaily.data.entity.UserKeyRecord
import com.smart.autodaily.data.entity.resp.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface UserKeyRecordApi {

    @GET("/getCoinRecord")
    suspend fun getInputKeyRecord(@Query("userId") userId : Int,@Query("page") page : Int, @Query("pageSize") pageSize : Int) : Response<List<UserKeyRecord>>

}