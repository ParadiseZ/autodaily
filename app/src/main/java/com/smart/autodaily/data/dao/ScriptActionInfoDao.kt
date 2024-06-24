package com.smart.autodaily.data.dao

import androidx.room.Dao
import androidx.room.Query
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

    @Query("select " +
            "a.id, a.script_id, a.set_id, b.set_value, a.pic_id, a.action_string, d.pic_path, c.pic_path" +
            " FROM script_action_info a " +
            "inner join script_set_info b on a.set_id = b.set_id and a.script_id = b.script_id " +
            "inner join pic_info c on a.pic_id = c.id " +
            "inner join script_info d on b.script_id = b.script_id " +
            "where b.checked_flag = 1 and b.set_type like 'SLIDER%' and a.script_id =:scriptId " +
            "union all " +
            "select " +
            "a.id, a.script_id, a.set_id, a.set_value, a.pic_id, a.action_string, d.pic_path, c.pic_path" +
            " FROM script_action_info a " +
            "inner join script_set_info b on a.set_id = b.set_id and a.script_id = b.script_id and a.set_value = b.set_value " +
            "inner join pic_info c on a.pic_id = c.id " +
            "inner join script_info d on b.script_id = b.script_id " +
            "where b.checked_flag = 1 and b.set_type not like 'SLIDER%' and a.script_id =:scriptId ")
    fun getCheckedByScriptId(scriptId: Int) : ArrayList<ScriptActionInfo>
}