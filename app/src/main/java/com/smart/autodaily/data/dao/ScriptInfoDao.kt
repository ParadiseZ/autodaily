package com.smart.autodaily.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.smart.autodaily.data.entity.ScriptInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface ScriptInfoDao {
    @Query("SELECT * FROM Script_Info  ORDER BY script_id LIMIT :pageSize OFFSET :starIndex")
    fun getScriptInfoByPage(pageSize : Int, starIndex : Int) : List<ScriptInfo>

    @Query("SELECT * FROM Script_Info where script_name like  :scriptName || '%' ORDER BY script_id LIMIT :pageSize OFFSET :starIndex")
    fun getScriptInfoByPage(scriptName : String,pageSize : Int, starIndex : Int) : List<ScriptInfo>

    @Query("SELECT script_id FROM Script_Info  ORDER BY script_id")
    fun getScriptIdAll() : List<Int>

    @Query("SELECT * FROM Script_Info WHERE script_id = :scriptId")
    fun getScriptInfoByScriptId(scriptId : Int) : ScriptInfo

    @Insert
    fun insert( scriptInfo: ScriptInfo ) : Unit

    @Update
    fun update( scriptInfo: ScriptInfo ) : Unit
}