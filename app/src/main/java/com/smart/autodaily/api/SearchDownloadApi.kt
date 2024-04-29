package com.smart.autodaily.api

import com.smart.autodaily.data.entity.ScriptInfo
import com.smart.autodaily.data.entity.ScriptSetInfo
import com.smart.autodaily.data.entity.resp.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface SearchDownloadApi {
    @GET("/query/getScriptByPage")
    suspend fun getAllScriptByPage(@Query("userName") userName : String, @Query("page") page : Int, @Query("pageSize") pageSize : Int) : Response<List<ScriptInfo>>

    @GET("/query/getScriptByPage")
    suspend fun getAllScriptByPage(
        @Query("userName") userName : String,
        @Query("scriptName") scriptName : String,
        @Query("page") page : Int,
        @Query("pageSize") pageSize : Int
    ) : Response<List<ScriptInfo>>

    @GET("/query/downloadScriptSetByScriptId")
    suspend fun downScriptSetByScriptId(@Query("scriptId") scriptId : Int) : Response<List<ScriptSetInfo>>

    @GET("/queryByPageTest")
    suspend fun getScript(@Query("userName") userName : String, @Query("page") page : Int, @Query("pageSize") pageSize : Int) : List<ScriptInfo>
}