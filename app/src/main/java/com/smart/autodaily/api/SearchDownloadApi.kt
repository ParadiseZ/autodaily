package com.smart.autodaily.api

import com.smart.autodaily.data.entity.ScriptActionInfo
import com.smart.autodaily.data.entity.ScriptInfo
import com.smart.autodaily.data.entity.ScriptSetInfo
import com.smart.autodaily.data.entity.VersionInfo
import com.smart.autodaily.data.entity.resp.Response
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Streaming

interface SearchDownloadApi {
    @GET("/query/getScriptByPage")
    suspend fun getAllScriptByPage(@Query("userId") userId : Int, @Query("page") page : Int, @Query("pageSize") pageSize : Int) : Response<List<ScriptInfo>>

    @GET("/query/getScriptByPage")
    suspend fun getAllScriptByPage(
        @Query("userId") userId : Int,
        @Query("scriptName") scriptName : String,
        @Query("page") page : Int,
        @Query("pageSize") pageSize : Int
    ) : Response<List<ScriptInfo>>

    @POST("/query/checkUpdateByIdAndVer")
    suspend fun checkUpdateByIdAndVer(@Body scriptInfoList : Map<Int, String>) : Response<Map<Int, VersionInfo>>

    @GET("/query/dlScriptInfoByScriptId")
    suspend fun dlScriptInfo(@Query("scriptId") scriptId : Int) : Response<ScriptInfo>

    @GET("/query/downloadScriptSetByScriptId")
    suspend fun downScriptSetByScriptId(@Query("scriptId") scriptId : Int) : Response<List<ScriptSetInfo>>

    @GET("/query/downloadActionInfoByScriptId")
    suspend fun downloadActionInfoByScriptId(@Query("scriptId") scriptId : Int) : Response<List<ScriptActionInfo>>

    @GET("/query/downloadModel")
    @Streaming
    fun downloadModel(@Query("scriptId") scriptId : Int, @Query("fileName")type : String): Call<ResponseBody>
}