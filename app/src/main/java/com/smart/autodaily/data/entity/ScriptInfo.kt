package com.smart.autodaily.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "script_info")
data class ScriptInfo(
    @PrimaryKey @ColumnInfo(name = "script_id") @SerializedName("script_id") val scriptId: Int,
    @ColumnInfo(name = "script_name") @SerializedName("script_name")var scriptName: String,  // 脚本名称
    @ColumnInfo(name = "script_version") @SerializedName("script_version")var scriptVersion: String, // 脚本版本号
    @ColumnInfo(name = "last_version") @SerializedName("last_version")  val lastVersion: String,   // 最新版本号
    @ColumnInfo(name = "checked_flag") @SerializedName("checked_flag")var checkedFlag: Boolean,     // 是否选中
    @ColumnInfo(name = "expiration_time") @SerializedName("expiration_time")var expirationTime: String?,    // 到期时间
    @ColumnInfo(name = "package_name") @SerializedName("package_name")val packageName: String?, // 包名
    @ColumnInfo(name = "runs_max_num") @SerializedName("runs_max_num")var runsMaxNum: Int = 1, // 最大运行次数
    @ColumnInfo(name = "next_run_date") @SerializedName("next_run_date")var nextRunDate: String,   // 下次运行日期
    @ColumnInfo(name = "download_time") @SerializedName("download_time")var downloadTime: String, // 下载日期
    @ColumnInfo(name = "is_downloaded") var isDownloaded: Int = 0, // 是否已下载

    @ColumnInfo(name = "current_status") @SerializedName("current_status")var currentStatus: Int = 0, // 脚本状态
    @ColumnInfo(name = "add_time") @SerializedName("add_time")var addTime: String?, // 添加日期
    @ColumnInfo(name = "update_time") @SerializedName("update_time")var updateTime: String?  //更新日期
)
