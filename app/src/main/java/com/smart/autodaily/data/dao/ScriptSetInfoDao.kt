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
    fun insert(setInfoListi: List<ScriptSetInfo>)

    @Query("select * from script_set_info where script_id = :scriptId order by set_id LIMIT :pageSize OFFSET :starIndex")
    fun queryScriptSetInfo(scriptId: Int, pageSize: Int, starIndex: Int) : List<ScriptSetInfo>

    @Query("select * from script_set_info where script_id = :scriptId and set_level=0 order by set_id")
    fun getScriptSetByScriptIdLv0(scriptId: Int) : List<ScriptSetInfo>

    @Query("select set_id from script_set_info where set_id = :setId or set_parent_id = :setId order by set_id")
    fun getScriptSetParentAndChild(setId: Int) : List<Int>

    @Query("select count(script_id) from script_set_info where script_id = :scriptId")
    fun countScriptSetByScriptId(scriptId: Int) : Int

    @Query("delete from script_set_info where script_id = :scriptId")
    fun deleteScriptSetInfoByScriptId(scriptId: Int)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(scriptSetInfo: ScriptSetInfo)

    @Query("update script_set_info set result_flag = :resultFlag where set_id = :setId")
    fun updateResultFlag(setId : Int, resultFlag : Boolean)
    @Query("update script_set_info set result_flag = :resultFlag where set_id = :setId or set_parent_id = :setId")
    fun updateParentAndChildResultFlag(setId : Int, resultFlag : Boolean)

    @Query("select result_flag from script_set_info where set_id = :setId")
    fun getResultFlag(setId : Int) : Boolean

    @Query("select result_flag from script_set_info where set_parent_id = :setParentId and result_flag = 0 and set_id!=0 limit 1")
    fun getChildResultFlag(setParentId : Int) : Boolean

    @Query("select set_value from script_set_info where set_id = :setId and script_id=0")
    fun getGlobalSetValueBySetId(setId : Int) : String

    @Query("select * from script_set_info where script_id=0 order by set_id")
    fun getGlobalSet() : List<ScriptSetInfo>
}