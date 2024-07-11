package com.smart.autodaily.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RoomWarnings
import androidx.room.Transaction
import com.smart.autodaily.data.entity.ScriptActionInfo

@Dao
interface ScriptActionInfoDao {

    @Query("SELECT * FROM script_action_info where set_id = :setId and script_id = :scriptId and set_value = :setValue")
    fun getSingle(setId: Int, scriptId: Int, setValue: String): ScriptActionInfo

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


    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query(
            "select a.* " +
            //"a.id, a.script_id, a.set_id, a.set_value, a.pic_id, a.action_string" +
            " FROM script_action_info a " +
            "inner join script_set_info b on a.set_id = b.set_id and a.script_id = b.script_id and a.set_value = b.set_value " +
            "where b.checked_flag = 1 and b.set_type not like 'SLIDER%' and a.script_id =:scriptId and a.set_id in( :setId )" +
            "union all " +
            //"select a.id, a.script_id, a.set_id, a.set_value, a.pic_id, a.action_string " +
            "select a.* " +
            "from script_action_info a where a.script_id = :scriptId and a.set_id = 0 "+
            "union all " +
            "select a.* " +
            "from script_action_info a " +
            "inner join script_set_info b on a.set_id = b.set_id and a.script_id = b.script_id " +
            "where a.script_id = :scriptId and b.set_type like 'SLIDER%' and a.set_id in( :setId ) "
    )
    fun getCheckedBySetId(setId: List<Int>, scriptId: Int) : List<ScriptActionInfo>
}