package com.smart.autodaily.data.repository

import com.smart.autodaily.api.SearchDownloadApi
import com.smart.autodaily.data.appDb
import com.smart.autodaily.data.entity.ScriptInfo
import com.smart.autodaily.data.dao.ScriptInfoDao
import com.smart.autodaily.data.dao.ScriptSetInfoDao
import com.smart.autodaily.retrofit2.RetrofitCreate
import kotlinx.coroutines.flow.Flow

 class ScriptRepository(
     private val scriptInfoDao: ScriptInfoDao,
     private val scriptSetInfoDao: ScriptSetInfoDao,
     private val searchDownloadApi: SearchDownloadApi
 ) {
    val scriptRepository  : ScriptRepository ?= null



    fun getScriptInfoById(uid : Int) : Flow<List<ScriptInfo>>{
        return scriptInfoDao.getScriptInfoById( uid )
    }
}