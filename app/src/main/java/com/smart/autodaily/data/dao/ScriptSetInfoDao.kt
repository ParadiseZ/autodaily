package com.smart.autodaily.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.smart.autodaily.data.entity.ScriptSetInfo

@Dao
interface ScriptSetInfoDao {
    @Insert
    fun insert(scriptSetInfo: ScriptSetInfo) : Unit

    @Query("select * from script_set_info where script_id = :script_id  LIMIT :pageSize OFFSET :starIndex")
    fun queryScriptSetInfo(script_id: Int, pageSize: Int, starIndex: Int) : List<ScriptSetInfo>

    @Query("select count(script_id) from script_set_info where script_id = :script_id")
    fun countScriptSetByScriptId(script_id: Int) : Int

    @Query("delete from script_set_info where script_id = :script_id")
    fun deleteScriptSetInfoByScriptId(script_id: Int) : Unit

    @Update
    fun updateScriptSetInfo(scriptSetInfo: ScriptSetInfo) : Unit
}