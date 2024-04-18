package com.smart.autodaily.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.smart.autodaily.data.entity.ScriptInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface ScriptInfoDao {
    @Query("SELECT * FROM Script_Info  ORDER BY uid LIMIT :pageSize OFFSET :starIndex")
    fun getScriptInfoByPage(pageSize : Int, starIndex : Int) : List<ScriptInfo>

    @Query("SELECT script_id FROM Script_Info  ORDER BY uid")
    fun getScriptIdAll() : List<Int>

    @Query("SELECT * FROM Script_Info WHERE uid = :uid ORDER BY uid ")
    fun getScriptInfoById( uid : Int ) : Flow<List<ScriptInfo>>

    @Insert
    fun insert( scriptInfo: ScriptInfo ) : Unit
}