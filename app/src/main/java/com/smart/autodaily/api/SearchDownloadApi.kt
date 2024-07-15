package com.smart.autodaily.api

import com.smart.autodaily.data.entity.ScriptActionInfo
import com.smart.autodaily.data.entity.ScriptInfo
import com.smart.autodaily.data.entity.ScriptSetInfo
import com.smart.autodaily.data.entity.resp.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
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

    @POST("/query/checkUpdateByIdAndVer")
    suspend fun checkUpdateByIdAndVer(@Body scriptInfoList : Map<Int, String>) : Response<Map<Int, String>>

    @GET("/query/downloadScriptSetByScriptId")
    suspend fun downScriptSetByScriptId(@Query("scriptId") scriptId : Int) : Response<List<ScriptSetInfo>>

    @GET("/query/downloadActionInfoByScriptId")
    suspend fun downloadActionInfoByScriptId(@Query("scriptId") scriptId : Int) : Response<List<ScriptActionInfo>>

    @GET("/queryByPageTest")
    suspend fun getScript(@Query("userName") userName : String, @Query("page") page : Int, @Query("pageSize") pageSize : Int) : List<ScriptInfo>
}