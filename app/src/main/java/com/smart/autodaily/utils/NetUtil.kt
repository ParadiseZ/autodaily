package com.smart.autodaily.utils

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetUtil {
    lateinit var okHttpClient: OkHttpClient

    private const val BASE_URL = "https://api.github.com/search/repositories?sort=stars&q=Android"       //根路径

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())     //添加用于解析数据的转换库
        .build()

    fun <T> create(serviceClass: Class<T>): T = retrofit.create(serviceClass) as T
}