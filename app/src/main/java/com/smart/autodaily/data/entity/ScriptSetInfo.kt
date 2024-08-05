package com.smart.autodaily.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.google.gson.annotations.SerializedName

@Entity(tableName = "script_set_info", primaryKeys = ["script_id", "set_id"])
data class ScriptSetInfo(
    @ColumnInfo(name = "script_id")@SerializedName("script_id") val scriptId: Int,
    @ColumnInfo(name = "set_id") @SerializedName("set_id") val setId: Int,
    @ColumnInfo(name = "checked_flag") @SerializedName("checked_flag") val checkedFlag: Boolean,
    @ColumnInfo(name = "set_type")  @SerializedName("set_type") val setType: SettingType,
    @ColumnInfo(name = "set_name") @SerializedName("set_name") val setName: String,
    @ColumnInfo(name = "set_desc") @SerializedName("set_desc") val setDesc: String?,
    @ColumnInfo(name = "set_parent_id") @SerializedName("set_parent_id") val setParentId: Int,
    @ColumnInfo(name = "set_level") @SerializedName("set_level") val setLevel: Int,
    @ColumnInfo(name = "set_default_value") @SerializedName("set_default_value") val setDefaultValue: String?,
    @ColumnInfo(name = "set_value") @SerializedName("set_value") var setValue: String?,
    @ColumnInfo(name = "set_range") @SerializedName("set_range") val setRange: String?,
    @ColumnInfo(name = "set_step") @SerializedName("set_step") val setStep: Int = 0,
    @ColumnInfo(name = "is_show") @SerializedName("is_show") val isShow: Boolean = true,
    @ColumnInfo(name = "result_flag") @SerializedName("result_flag") var resultFlag: Boolean = false,
    @ColumnInfo(name = "once_flag") @SerializedName("once_flag") val onceFlag: Boolean = false,
    @ColumnInfo(name = "add_time") @SerializedName("add_time") val addTime: String?,
    @ColumnInfo(name = "update_time") @SerializedName("update_time") var updateTime: String?
)


enum class SettingType {
    SWITCH, SLIDER, TEXT_FIELD, CHECK_BOX, RADIO_BUTTON, SLIDER_SECOND, TITLE, SLIDER_THIRD
}

const val WORK_TYPE01 = "无障碍+录屏(安卓7-12)"
const val WORK_TYPE02 = "shizuku"
const val WORK_TYPE03 = "root"