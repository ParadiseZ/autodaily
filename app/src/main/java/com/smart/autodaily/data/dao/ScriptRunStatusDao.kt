package com.smart.autodaily.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.smart.autodaily.data.entity.ScriptRunStatus

@Dao
interface ScriptRunStatusDao {
    @Insert
    suspend fun insert(scriptRunStatus: ScriptRunStatus)

    @Update
    suspend fun update(scriptRunStatus: ScriptRunStatus)

    @Query("SELECT count(1) FROM SCRIPT_RUN_STATUS WHERE script_id = :scriptId AND flow_id_type = :flowIdType and cur_status=2 and date_time =:dateTime")
    suspend fun countByFlowIdAndType(scriptId : Int, flowIdType : Int, dateTime : String): Int

    @Query("delete FROM SCRIPT_RUN_STATUS WHERE date_time<:dateTime")
    suspend fun deleteStatus(dateTime: String)

    @Query("delete FROM SCRIPT_RUN_STATUS WHERE script_id = :scriptId")
    suspend fun deleteStatus(scriptId: Int): Int
}