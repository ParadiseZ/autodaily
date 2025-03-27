package com.smart.autodaily.retrofit2

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitCreate {
    //private const val BASE_URL = "http://192.168.123.208:8080"
    private const val BASE_URL = "https://autodaily.icu/"
    //private const val MEDIA_TYPE = "application/json"
    //private const val BASE_URL = "https://www.wanandroid.com/"
    //private val  okHttpClient = OkHttpClient.Builder().build()
    private val okHttpClient = OkHttpClient.Builder()
        .callTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
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