package com.smart.autodaily.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.smart.autodaily.data.entity.ScriptInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface ScriptInfoDao {
    @Query("SELECT * FROM Script_Info ORDER BY uid ")
    fun getScriptInfoFlow() : Flow<List<ScriptInfo>>

    @Query("SELECT * FROM Script_Info WHERE uid = :uid ORDER BY uid ")
    fun getScriptInfoById( uid : Int ) : Flow<List<ScriptInfo>>
}