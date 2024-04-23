package com.smart.autodaily.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.smart.autodaily.data.entity.ScriptSetInfo

@Dao
interface ScriptSetInfoDao {
    @Insert
    fun insert(scriptSetInfo: ScriptSetInfo) : Unit

    @Query("select * from script_set_info where script_id = :script_id")
    fun queryScriptSetInfo(script_id: Int) : List<ScriptSetInfo>

    @Query("delete from script_set_info where script_id = :script_id")
    fun deleteScriptSetInfoByScriptId(script_id: Int) : Unit
}