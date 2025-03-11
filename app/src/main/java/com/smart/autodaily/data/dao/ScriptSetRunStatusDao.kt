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

    @Query("SELECT count(1) FROM script_set_run_status WHERE scriptId = :scriptId and flowId = :flowId AND flowIdType = :flowIdType and curStatus=2 and dateTime =:dateTime")
    suspend fun countByFlowIdAndType(scriptId : Int,flowId: Int, flowIdType : Int, dateTime : String): Int

    @Query("delete FROM script_set_run_status WHERE dateTime<:dataTime")
    suspend fun deleteStatus(dataTime:  String)

    @Query("delete FROM script_set_run_status WHERE scriptId = :scriptId")
    suspend fun deleteStatus(scriptId: Int): Int
}