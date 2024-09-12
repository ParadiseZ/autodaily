package com.smart.autodaily.api

import com.smart.autodaily.retrofit2.RetrofitCreate


object RemoteApi {
    val searchDownRetrofit : SearchDownloadApi = RetrofitCreate.create<SearchDownloadApi>()
    val registerLoginRetrofit : RegisterLoginApi = RetrofitCreate.create<RegisterLoginApi>()
    val userKeyRetrofit : UserKeyApi = RetrofitCreate.create<UserKeyApi>()
    val runRetrofit : RunApi = RetrofitCreate.create<RunApi>()
    val updateRetrofit : UpdateApi = RetrofitCreate.create<UpdateApi>()

    val keyTypeApi : KeyTypeApi = RetrofitCreate.create<KeyTypeApi>()
    val virtualCoinApi : VirtualCoinApi = RetrofitCreate.create<VirtualCoinApi>()
    val userKeyRecordApi : UserKeyRecordApi = RetrofitCreate.create<UserKeyRecordApi>()
}