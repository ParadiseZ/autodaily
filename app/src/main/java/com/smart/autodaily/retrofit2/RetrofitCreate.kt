package com.smart.autodaily.retrofit2

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitCreate {
    //private const val BASE_URL = "http://192.168.191.244:8080"
    private const val BASE_URL = "http://192.168.123.208:8080"
    //private const val BASE_URL = "https://www.wanandroid.com/"
    private val retrofit = Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(
        GsonConverterFactory.create()
    ).build()
    fun <T> create(serviceClass: Class<T>):T = retrofit.create(serviceClass)
    inline fun <reified T> create():T = create(T::class.java)
}