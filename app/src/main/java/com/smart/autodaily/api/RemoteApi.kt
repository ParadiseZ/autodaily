package com.smart.autodaily.api

import com.smart.autodaily.retrofit2.RetrofitCreate


object RemoteApi {
    val searchDownRetrofit : SearchDownloadApi = RetrofitCreate.create<SearchDownloadApi>()
    val registerLoginRetrofit : RegisterLoginApi = RetrofitCreate.create<RegisterLoginApi>()
    val userKeyRetrofit : UserKeyApi = RetrofitCreate.create<UserKeyApi>()
}