package com.smart.autodaily.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "script_info")
data class ScriptInfoNet(
    @PrimaryKey
    @ColumnInfo(name = "script_id") val scriptId: Int,
    @ColumnInfo(name = "script_name") var scriptName: String,  // 脚本名称
    @ColumnInfo(name = "script_version") var scriptVersion: String, // 脚本版本号
    @ColumnInfo(name = "last_version") val lastVersion: String,   // 最新版本号
    @ColumnInfo(name = "checked_flag") var checkedFlag: Boolean,     // 是否选中
    @ColumnInfo(name = "expiration_time") var expirationTime: String,    // 到期时间
    @ColumnInfo(name = "package_name") val packageName: String = "", // 包名
    @ColumnInfo(name = "runs_max_num") var runsMaxNum: Int = 1, // 最大运行次数
    @ColumnInfo(name = "next_run_date") var nextRunDate: String,   // 下次运行日期
    @ColumnInfo(name = "download_time") var downloadTime: String // 下载日期
)
