package com.smart.autodaily.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "app_info", primaryKeys = ["id"])
data class AppInfo(
    @ColumnInfo(name = "id") val id : Int,
    @ColumnInfo(name = "type") val type : String,
    @ColumnInfo(name = "value") var value : Boolean,
    @ColumnInfo(name = "desc") val desc : String
)