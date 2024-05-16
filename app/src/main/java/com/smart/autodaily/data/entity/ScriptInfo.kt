package com.smart.autodaily.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "script_info")
data class ScriptInfo(
    @PrimaryKey @ColumnInfo(name = "script_id") val script_id: Int,
    @ColumnInfo(name = "script_name") var script_name: String,  // 脚本名称
    @ColumnInfo(name = "script_version") var script_version: String, // 脚本版本号
    @ColumnInfo(name = "last_version") val last_version: String,   // 最新版本号
    @ColumnInfo(name = "checked_flag") var checked_flag: Boolean,     // 是否选中
    @ColumnInfo(name = "expiration_time") var expiration_time: String?,    // 到期时间
    @ColumnInfo(name = "package_name") val package_name: String?, // 包名
    @ColumnInfo(name = "runs_max_num") var runsMaxNum: Int = 1, // 最大运行次数
    @ColumnInfo(name = "next_run_date") var next_run_date: String,   // 下次运行日期
    @ColumnInfo(name = "download_time") var download_time: String, // 下载日期
    @ColumnInfo(name = "is_downloaded") var is_downloaded: Int = 0, // 是否已下载
)
