package com.smart.autodaily.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.smart.autodaily.data.entity.ScriptActionInfo

@Dao
interface ScriptActionInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(saiList: List<ScriptActionInfo>)

    @Query("SELECT * FROM script_action_info where flow_id = :setId and script_id = :scriptId and is_valid=1 and set_value = :setValue")
    fun getSingle(setId: Int, scriptId: Int, setValue: String): ScriptActionInfo

    @Query("DELETE FROM script_action_info where script_id = :scriptId")
    fun deleteByScriptId(scriptId: Int)

    @Transaction
    fun getActionInfo(condition : List<Triple<Int, Int, String>>): ArrayList<ScriptActionInfo>{
        val result = arrayListOf<ScriptActionInfo>()
        condition.forEach {
            result += getSingle(it.first, it.second, it.third).apply {
                it.third.split(",").toTypedArray()
            }
        }
        return result
    }

    @Query("update script_action_info set execute_max = :executeMax where script_id = :scriptId and id in (:acIds)")
    fun updateExecuteMax(scriptId : Int, acIds:List<Int>,executeMax:Int)

/*    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("select " +
            "a.id, a.script_id, a.set_id, b.set_value, a.pic_id, a.action_string, d.pic_path as picPath" +
            " FROM script_action_info a " +
            "inner join script_set_info b on a.set_id = b.set_id and a.script_id = b.script_id " +

            "inner join script_info d on b.script_id = b.script_id " +
            "where b.checked_flag = 1 and b.set_type like 'SLIDER%' and a.script_id =:scriptId " +
            "union all " +
            "select " +
            "a.id, a.script_id, a.set_id, a.set_value, a.pic_id, a.action_string, d.pic_path  as picPath" +
            " FROM script_action_info a " +
            "inner join script_set_info b on a.set_id = b.set_id and a.script_id = b.script_id and a.set_value = b.set_value " +

            "inner join script_info d on b.script_id = b.script_id " +
            "where b.checked_flag = 1 and b.set_type not like 'SLIDER%' and a.script_id =:scriptId ")
    fun getCheckedByScriptId(scriptId: Int) : List<ScriptActionInfo>*/

    @Query("select * from (" +
            "select a.*  FROM script_action_info a, script_set_info b where b.checked_flag=1 and b.script_id=:scriptId and " +
            "(b.flow_id in (:flowIds) or b.flow_parent_id like '0%') and b.flow_id_type = :flowIdType and " +
            "a.flow_id = b.flow_id and a.script_id = b.script_id  and  (a.set_value = b.set_value or a.set_value is null) and a.is_valid=1 " +
            "union  " +
            "select a.*  FROM script_action_info a where a.flow_id=0 and a.script_id = :scriptId" +
            ") order by script_id,sort ")
    fun getCheckedBySetId(scriptId: Int,flowIds: List<Int>, flowIdType: Int) : List<ScriptActionInfo>

    @Query("select *  FROM script_action_info where script_id = :scriptId and (flow_id in (" +
            " select a.flow_id  FROM script_set_info a where a.script_id = :scriptId and a.back_flag = 1 "+
            ") or flow_id<0)  and is_valid=1 order by script_id,sort")
    fun getBackActions(scriptId: Int) : List<ScriptActionInfo>

    @Query("select flow_id from script_action_info where id = :id and is_valid=1")
    fun getCurFlowIdById(id : Int) : Int
}