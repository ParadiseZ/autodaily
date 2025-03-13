package com.smart.autodaily.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(tableName = "script_set_run_status",indices = [Index(value = ["flow_id", "flow_id_type","date_time"])])
data class ScriptSetRunStatus(
    @PrimaryKey(autoGenerate = true) val id : Int = 0,
    @ColumnInfo("script_id") val scriptId : Int,
    @ColumnInfo("flow_id") val flowId : Int,
    @ColumnInfo("flow_id_type") val flowIdType : Int,
    @ColumnInfo("cur_status") val curStatus: Int,
    @ColumnInfo("date_time") val dateTime: String
)
