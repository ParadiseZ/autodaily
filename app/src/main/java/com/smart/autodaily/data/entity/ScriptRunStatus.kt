package com.smart.autodaily.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "script_run_status" ,indices = [
    Index(value = ["date_time"]),
    Index(value = ["script_id", "flow_id_type"])])
data class ScriptRunStatus(
    @PrimaryKey(autoGenerate = true) val id : Int = 0,
    @ColumnInfo("script_id") val scriptId : Int,
    @ColumnInfo("flow_id_type") val flowIdType : Int,
    @ColumnInfo("cur_status") val curStatus: Int,
    @ColumnInfo("date_time") val dateTime: String
)
