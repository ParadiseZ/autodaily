package com.smart.autodaily.retrofit2

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitCreate {
    private const val BASE_URL = "http://192.168.123.208:9090"
    //private const val BASE_URL = "https://autodaily.icu/"
    //private const val MEDIA_TYPE = "application/json"

    private val okHttpClient = OkHttpClient.Builder()
        .callTimeout(330, TimeUnit.SECONDS)
        .connectTimeout(15,TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.MINUTES)    // 读取响应数据
        .writeTimeout(30, TimeUnit.SECONDS)   // 发送请求数据
        .addInterceptor(AuthInterceptor())
        .build()

    //private val request: Request = Request.Builder().url(BASE_URL).build()
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(
        GsonConverterFactory.create()
    ).build()
    fun <T> create(serviceClass: Class<T>):T = retrofit.create(serviceClass)
    inline fun <reified T> create():T = create(T::class.java)
}