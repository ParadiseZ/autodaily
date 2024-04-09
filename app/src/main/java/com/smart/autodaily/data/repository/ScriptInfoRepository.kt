package com.smart.autodaily.data.repository

import com.smart.autodaily.data.entity.ScriptInfo
import com.smart.autodaily.data.dao.ScriptInfoDao
import kotlinx.coroutines.flow.Flow

class ScriptInfoRepository (
    private val scriptInfoDao: ScriptInfoDao
){
    val scriptInfoFlow : Flow<List<ScriptInfo>>
        get() {
            return scriptInfoDao.getScriptInfoFlow()
        }

    fun getScriptInfoById(uid : Int) : Flow<List<ScriptInfo>>{
        return scriptInfoDao.getScriptInfoById( uid )
    }
}