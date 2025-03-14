package com.smart.autodaily.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.smart.autodaily.data.entity.ScriptSetInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface ScriptSetInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(setInfoListi: List<ScriptSetInfo>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(scriptSetInfo: ScriptSetInfo)

    //展示
    @Query("select * from script_set_info where script_id = :scriptId and is_show = 1 order by sort LIMIT :pageSize OFFSET :starIndex")
    fun queryScriptSetInfo(scriptId: Int, pageSize: Int, starIndex: Int) : List<ScriptSetInfo>

    @Query("select count(script_id) from script_set_info where script_id = :scriptId")
    fun countScriptSetByScriptId(scriptId: Int) : Int

    @Query("delete from script_set_info where script_id = :scriptId")
    fun deleteScriptSetInfoByScriptId(scriptId: Int)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(scriptSetInfo: ScriptSetInfo)

    @Query("update script_set_info set result_flag = :resultFlag where set_id = :setId")
    fun updateResultFlag(setId : Int, resultFlag : Boolean)

    @Query("select result_flag from script_set_info where set_id = :setId")
    fun getResultFlag(setId : Int) : Boolean

    @Query("select set_value from script_set_info where set_id = :setId and script_id=0")
    fun getGlobalSetValueBySetId(setId : Int) : String

    //获取全局设置
    @Query("select * from script_set_info where script_id=0 and is_show=1 order by sort")
    fun getGlobalSet() : List<ScriptSetInfo>

    //获取脚本全局设置
    @Query("select * from script_set_info where script_id=:scriptId and flow_id=0")
    fun getScriptGlobalSet(scriptId: Int) :List<ScriptSetInfo>

    //去掉为0的，全局设置的
    @Query("select * from script_set_info where script_id=:scriptId and checked_flag=1 and is_max_level = :isMaxLevel and flow_id>=:curFlowId and flow_parent_id not like '0%'")
    fun getScriptSetByScriptId(scriptId: Int,curFlowId : Int, isMaxLevel : Int) :MutableList<ScriptSetInfo>
    //去掉隐藏的，这里提供给用户查看
    @Query("select * from script_set_info where script_id=:scriptId and is_show= 1 order by sort")
    fun getScriptSetByScriptId(scriptId: Int) : Flow<List<ScriptSetInfo>>

    @Query("select count(1) from script_set_info where script_id=:scriptId and checked_flag=1 and flow_id in (:flowIds)")
    fun countCheckedNumByParentFlowId(scriptId: Int ,flowIds : List<Int>) : Int

    @Query("select count(1) from script_set_info where script_id=:scriptId and checked_flag=1 and flow_id>=:curFlowId and flow_parent_id like  :flowParentId||',%'")
    fun getChildCheckedCount(scriptId: Int ,curFlowId : Int,flowParentId : String) : Int

    @Query("select a.flow_id  FROM script_set_info a where a.script_id = :scriptId and a.back_flag = 1")
    fun getBackSetByScriptId(scriptId: Int) : List<Int>

    @Query("select *  FROM script_set_info a where a.script_id = :scriptId and a.flow_id = :flowId")
    fun getScriptSetByFlowId(scriptId: Int, flowId : Int) : List<ScriptSetInfo>

    @Query("select *  FROM script_set_info a where a.set_id = :setId")
    fun getScriptSetById(setId: Int) : ScriptSetInfo?
}