package com.smart.autodaily.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "script_info")
data class ScriptInfo(
    @PrimaryKey(autoGenerate = true) val uid : Int,
    @ColumnInfo(name = "Script_Id") val scriptId : Int,
    @ColumnInfo(name = "Script_Name") var scriptName : String,  //脚本名称
    @ColumnInfo(name = "Script_Version") var scriptVersion: String, //脚本版本号
    @ColumnInfo(name = "Last_Version") val lastVersion : String,   //最新版本号
    @ColumnInfo(name = "Checked_Flag") var checkedFlag : Boolean,     //是否选中
    @ColumnInfo(name = "Expiration_Time") var expirationTime : String,    //到期时间
    @ColumnInfo(name = "Package_Name") val packageName : String = "", //包名
    @ColumnInfo(name = "Runs_Max_Num") var runsMaxNum : Int = 1, //最大运行次数
    @ColumnInfo(name = "Next_Run_Date") var nextRunDate : String,   //下次运行日期
    @ColumnInfo(name = "DOWNLOAD_TIME") var downLoadTime : String //下载日期
)
