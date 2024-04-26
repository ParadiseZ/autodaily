package com.smart.autodaily.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "script_set_info", primaryKeys = ["script_id", "set_id"])
data class ScriptSetInfo(
    @ColumnInfo(name = "script_id") val script_id: Int,
    @ColumnInfo(name = "set_id") val set_id: Int,
    @ColumnInfo(name = "checked_flag") val checked_flag: Boolean,
    @ColumnInfo(name = "set_type") val set_type: SettingType,
    @ColumnInfo(name = "set_name") val set_name: String,
    @ColumnInfo(name = "set_desc") val set_desc: String,
    @ColumnInfo(name = "set_parent_id") val set_parent_id: Int,
    @ColumnInfo(name = "set_level") val set_level: Int,
    @ColumnInfo(name = "set_default_value") val set_default_value: String,
    @ColumnInfo(name = "set_value") var set_value: String,
    @ColumnInfo(name = "is_show") val is_show: Boolean,
    @ColumnInfo(name = "result_flag") val result_flag: Boolean,
    @ColumnInfo(name = "once_flag") val once_flag: Boolean
)


enum class SettingType {
    SWITCH, SLIDER, TEXT_FIELD, CHECK_BOX, RADIO_BUTTON
}