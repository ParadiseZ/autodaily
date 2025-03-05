package com.smart.autodaily.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(tableName = "script_set_run_status",indices = [Index(value = ["flowId", "flowIdType","dateTime"])])
data class ScriptSetRunStatus(
    @PrimaryKey(autoGenerate = true) val id : Int = 0,
    val scriptId : Int,
    val flowId : Int,
    val flowIdType : Int,
    val curStatus: Int,
    val dateTime: String
)
