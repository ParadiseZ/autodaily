package com.smart.autodaily.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.smart.autodaily.data.entity.ScriptRunStatus

@Dao
interface ScriptRunStatusDao {
    @Insert
    fun insert(scriptRunStatus: ScriptRunStatus)

    @Update
    fun update(scriptRunStatus: ScriptRunStatus)

    @Query("SELECT count(1) FROM script_run_status WHERE flowId = :flowId AND flowIdType = :flowIdType and curStatus=2 and dateTime =:dateTime")
    fun countByFlowIdAndType(flowId: Int, flowIdType : Int, dateTime : String): Int
}