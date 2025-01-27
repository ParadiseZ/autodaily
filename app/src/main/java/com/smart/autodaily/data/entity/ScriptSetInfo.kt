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
    @ColumnInfo(name = "checked_flag") @SerializedName("checked_flag") var checkedFlag: Boolean,//是否选择
    @ColumnInfo(name = "set_type")  @SerializedName("set_type") val setType: SettingType,//设置类型，以此判断用什么UI组件
    @ColumnInfo(name = "set_name") @SerializedName("set_name") val setName: String,
    @ColumnInfo(name = "set_desc") @SerializedName("set_desc") val setDesc: String?,
    @ColumnInfo(name = "set_level") @SerializedName("set_level") val setLevel: Int,
    @ColumnInfo(name = "set_default_value") @SerializedName("set_default_value") val setDefaultValue: String?,//可选设置值，以,分割
    @ColumnInfo(name = "set_value") @SerializedName("set_value") var setValue: String?,//set_default_value其中一个值
    @ColumnInfo(name = "set_range") @SerializedName("set_range") val setRange: String?,//滑块类型UI范围
    @ColumnInfo(name = "set_step") @SerializedName("set_step") val setStep: Int=0,//滑块类型UI总步数
    @ColumnInfo(name = "flow_id") @SerializedName("flow_id") val flowId: Int?,//流程id
    @ColumnInfo(name = "is_show") @SerializedName("is_show") val isShow: Int = 1,//是否显示
    @ColumnInfo(name = "result_flag") @SerializedName("result_flag") var resultFlag: Boolean = false,//执行结果标志
    @ColumnInfo(name = "add_time") @SerializedName("add_time") val addTime: String?,
    @ColumnInfo(name = "update_time") @SerializedName("update_time") var updateTime: String?,
    @ColumnInfo(name = "sort") @SerializedName("sort") var sort: Int,//排序方式，展示时使用
    @ColumnInfo(name = "is_max_level") @SerializedName("is_max_level") val isMaxLevel: Int = 0,//是否最底层，1代表需要执行本条设置，2代表如果选择，此时子设置如果未选择则执行本设置，选择则执行子设置（即流程，比如本设置是扫荡副本，子设置是否购买体力）
    @ColumnInfo(name = "flow_parent_id") @SerializedName("flow_parent_id") val flowParentId: String?,//父流程id,以此查询组合哪些script_action_info表中的流程操作
    @ColumnInfo(name = "flow_id_type") @SerializedName("flow_id_type") val flowIdType: Int,//流程类型，以此判定本时间段，是否执行本设置。比如晚上执行全部设置，但早上只执行一部分，两条设置基本一致
    @ColumnInfo(name = "back_flag") @SerializedName("back_flag") val backFlag: Int = 0//是否是返回类操作，以此将其整合到返回操作集合，在非正常流程时尝试执行
){
    @Ignore
    var flowParentIdList : List<Int> = emptyList()
}

