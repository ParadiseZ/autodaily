package com.smart.autodaily.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(tableName = "script_run_status",indices = [Index(value = ["flowId", "flowIdType","dateTime"])])
data class ScriptRunStatus(
    @PrimaryKey(autoGenerate = true) val id : Int = 0,
    val flowId : Int,
    val flowIdType : Int,
    val curStatus: Int,
    val dateTime: String
)
