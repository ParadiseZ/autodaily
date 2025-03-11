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

    @Query("SELECT count(scriptId) FROM SCRIPT_RUN_STATUS WHERE scriptId = :scriptId AND flowIdType = :flowIdType and curStatus=2 and dateTime =:dateTime")
    suspend fun countByFlowIdAndType(scriptId : Int, flowIdType : Int, dateTime : String): Int

    @Query("delete FROM SCRIPT_RUN_STATUS WHERE dateTime<:dateTime")
    suspend fun deleteStatus(dateTime: String)

    @Query("delete FROM SCRIPT_RUN_STATUS WHERE scriptId = :scriptId")
    suspend fun deleteStatus(scriptId: Int): Int
}