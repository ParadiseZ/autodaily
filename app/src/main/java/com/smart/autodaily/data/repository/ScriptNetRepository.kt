package com.smart.autodaily.data.repository

import com.smart.autodaily.api.SearchDownloadApi
import com.smart.autodaily.retrofit2.RetrofitCreate


 object ScriptNetRepository {
     private val retrofit = RetrofitCreate.create<SearchDownloadApi>()
     suspend fun getScriptByPage(
         userName:String
     ) = retrofit
         .getAllScriptByPage(
             userName
         )
}