package com.smart.autodaily.api

import com.smart.autodaily.data.entity.ScriptInfo
import com.smart.autodaily.data.entity.resp.ScriptNetSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface SearchDownloadApi {
    @GET("/queryScriptListAll")
    suspend fun getAllScriptByPage(@Query("userName") userName : String) : ScriptNetSearchResponse<ScriptInfo>
}