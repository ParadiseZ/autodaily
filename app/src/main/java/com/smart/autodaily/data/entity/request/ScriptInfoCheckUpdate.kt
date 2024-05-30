package com.smart.autodaily.data.entity.request

import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName

data class ScriptInfoCheckUpdate (
    @ColumnInfo(name = "script_id") @SerializedName("script_id") val scriptId: Int,
    @ColumnInfo(name = "last_version") @SerializedName("last_version")  val lastVersion: String,   // 最新版本号
)