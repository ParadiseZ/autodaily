package com.smart.autodaily.data.entity

import androidx.compose.runtime.mutableIntStateOf
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import com.google.gson.annotations.SerializedName

@Entity(tableName = "script_info", primaryKeys = ["script_id"])
data class ScriptInfo(
    @ColumnInfo(name = "script_id") @SerializedName("script_id") val scriptId: Int,
    @ColumnInfo(name = "script_name") @SerializedName("script_name")var scriptName: String,  // 脚本名称
    @ColumnInfo(name = "last_version") @SerializedName("last_version") var lastVersion: Int?,   // 最新版本号
    //@ColumnInfo(name = "expiration_time") @SerializedName("expiration_time")var expirationTime: String?,    // 到期时间
    @ColumnInfo(name = "package_name") @SerializedName("package_name")val packageName: String, // 包名
    @ColumnInfo(name = "runs_max_num") @SerializedName("runs_max_num")var runsMaxNum: Int = 1, // 最大运行次数
    @ColumnInfo(name = "current_status") @SerializedName("current_status") var currentStatus: Int = 0, // 脚本状态
    @ColumnInfo(name = "add_time") @SerializedName("add_time") var addTime: String, // 添加日期
    @ColumnInfo(name = "update_time") @SerializedName("update_time") var updateTime: String?,  //更新日期
    @ColumnInfo(name = "model_path") @SerializedName("model_path") val modelPath: String, // 模型路径
    @ColumnInfo(name = "classes_num") @SerializedName("classes_num") var classesNum: Int, // 类别数量
    @ColumnInfo(name = "img_size") @SerializedName("img_size") var imgSize: Int = 640, // 类别数量

    @ColumnInfo(name = "checked_flag") var checkedFlag: Boolean = true,     // 是否选中
    @ColumnInfo(name = "script_version") var scriptVersion: Int = 0, // 脚本版本号
    @ColumnInfo(name = "next_run_date") var nextRunDate: String?,   // 下次运行日期
    @ColumnInfo(name = "download_time") var downloadTime: String, // 下载日期
    @ColumnInfo(name = "is_downloaded") var isDownloaded: Int = 0, // 是否已下载
    @ColumnInfo(name = "current_run_num") var currentRunNum: Int = 0, // 当前运行次数
    @ColumnInfo(name = "need_app_update") var needAppUpdate: Int = 0, // 当前运行次数
    @ColumnInfo(name = "lang") var lang: Int = 0, // 语言
){
    @Ignore
    var process = mutableIntStateOf(-2)  //下载进度条
    @Ignore
    var downState = mutableIntStateOf(isDownloaded)  //下载标志
}
