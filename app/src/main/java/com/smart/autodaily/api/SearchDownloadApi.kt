package com.smart.autodaily.api

import com.smart.autodaily.data.entity.resp.ScriptNetSearchResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface SearchDownloadApi {
    @GET("queryScriptListAll")
    suspend fun getAllScriptByPage(@Query("user") userName : String, @Query("page") page: Int, @Query("per_page") perPage: Int) : ScriptNetSearchResponse

    companion object {
        //private const val BASE_URL = "http://127.0.0.1:8080"
        private const val BASE_URL = "http://192.168.123.208:8080"

        fun create(): SearchDownloadApi {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(SearchDownloadApi::class.java)
        }
    }
}