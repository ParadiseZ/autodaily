package com.smart.autodaily.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.smart.autodaily.data.entity.ScriptSetRunStatus

@Dao
interface ScriptSetRunStatusDao {
    @Insert
    suspend fun insert(scriptSetRunStatus: ScriptSetRunStatus)

    @Update
    suspend fun update(scriptSetRunStatus: ScriptSetRunStatus)

    @Query("SELECT count(1) FROM script_set_run_status WHERE script_id = :scriptId and flow_id = :flowId AND flow_id_type = :flowIdType and cur_status=2 and date_time =:dateTime")
    suspend fun countByFlowIdAndType(scriptId : Int,flowId: Int, flowIdType : Int, dateTime : String): Int

    @Query("delete FROM script_set_run_status WHERE date_time<:dataTime")
    suspend fun deleteStatus(dataTime:  String)

    @Query("delete FROM script_set_run_status WHERE script_id = :scriptId")
    suspend fun deleteStatus(scriptId: Int): Int
}