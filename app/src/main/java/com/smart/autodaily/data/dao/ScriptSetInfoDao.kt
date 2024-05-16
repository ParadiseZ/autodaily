package com.smart.autodaily.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.smart.autodaily.data.entity.ScriptSetInfo

@Dao
interface ScriptSetInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(scriptSetInfo: ScriptSetInfo)

    @Query("select * from script_set_info where script_id = :scriptId  LIMIT :pageSize OFFSET :starIndex")
    fun queryScriptSetInfo(scriptId: Int, pageSize: Int, starIndex: Int) : List<ScriptSetInfo>

    @Query("select count(script_id) from script_set_info where script_id = :scriptId")
    fun countScriptSetByScriptId(scriptId: Int) : Int

    @Query("delete from script_set_info where script_id = :scriptId")
    fun deleteScriptSetInfoByScriptId(scriptId: Int)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateScriptSetInfo(scriptSetInfo: ScriptSetInfo)
}