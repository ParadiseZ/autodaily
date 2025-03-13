package com.smart.autodaily.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.google.gson.annotations.SerializedName

@Entity(tableName = "app_info", primaryKeys = ["id"])
data class AppInfo(
    @ColumnInfo(name = "id") @SerializedName("id") val id : Int,
    @ColumnInfo(name = "config_name") @SerializedName("config_name")  val confName : String,
    @ColumnInfo(name = "config_value") @SerializedName("config_value") var confValue : String,
    @ColumnInfo(name = "config_desc") @SerializedName("config_desc") val confDesc : String
)