package com.smart.autodaily.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import com.google.gson.annotations.SerializedName
import com.smart.autodaily.constant.SettingType

@Entity(tableName = "script_set_info", primaryKeys = ["set_id"],indices = [Index(value = ["script_id", "set_value"])])
data class ScriptSetInfo(
    @ColumnInfo(name = "script_id")@SerializedName("script_id") val scriptId: Int,
    @ColumnInfo(name = "set_id") @SerializedName("set_id") val setId: Int,
    @ColumnInfo(name = "checked_flag") @SerializedName("checked_flag") var checkedFlag: Boolean,
    @ColumnInfo(name = "set_type")  @SerializedName("set_type") val setType: SettingType,
    @ColumnInfo(name = "set_name") @SerializedName("set_name") val setName: String,
    @ColumnInfo(name = "set_desc") @SerializedName("set_desc") val setDesc: String?,
    @ColumnInfo(name = "set_level") @SerializedName("set_level") val setLevel: Int,
    @ColumnInfo(name = "set_default_value") @SerializedName("set_default_value") val setDefaultValue: String?,
    @ColumnInfo(name = "set_value") @SerializedName("set_value") var setValue: String?,
    @ColumnInfo(name = "set_range") @SerializedName("set_range") val setRange: String?,
    @ColumnInfo(name = "set_step") @SerializedName("set_step") val setStep: Int=0,
    @ColumnInfo(name = "flow_id") @SerializedName("flow_id") val flowId: Int?,
    @ColumnInfo(name = "is_show") @SerializedName("is_show") val isShow: Boolean = true,
    @ColumnInfo(name = "result_flag") @SerializedName("result_flag") var resultFlag: Boolean = false,
    @ColumnInfo(name = "add_time") @SerializedName("add_time") val addTime: String?,
    @ColumnInfo(name = "update_time") @SerializedName("update_time") var updateTime: String?,
    @ColumnInfo(name = "sort") @SerializedName("sort") var sort: Int,
    @ColumnInfo(name = "is_max_level") @SerializedName("is_max_level") val isMaxLevel: Int = 0,
    @ColumnInfo(name = "flow_parent_id") @SerializedName("flow_parent_id") val flowParentId: String?,
    @ColumnInfo(name = "flow_id_type") @SerializedName("flow_id_type") val flowIdType: Int,
    @ColumnInfo(name = "back_flag") @SerializedName("back_flag") val backFlag: Int = 0
){
    @Ignore
    var flowParentIdList : List<Int> = emptyList()
}

