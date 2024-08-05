package com.smart.autodaily.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import com.google.gson.annotations.SerializedName
import com.smart.autodaily.command.Command

@Entity(tableName = "script_action_info", primaryKeys = ["id"], indices = [Index(value = ["script_id", "set_id","set_value"])])
data class ScriptActionInfo(
    @ColumnInfo(name = "id") @SerializedName("id") val id: Int,
    @ColumnInfo(name = "script_id") @SerializedName("script_id") val scriptId: Int,
    @ColumnInfo(name = "set_id") @SerializedName("set_id") val setId: Int,
    @ColumnInfo(name = "set_value") @SerializedName("set_value") var setValue: String,
    @ColumnInfo(name = "pic_id") @SerializedName("pic_id") val picId : String,
    @ColumnInfo(name = "action_string") @SerializedName("action_string") var actionString : String,
    //pic_info
    @ColumnInfo(name = "pic_name_list") @SerializedName("pic_name_list") val picNameList : String,
    //action_string
    @ColumnInfo(name = "pic_not_found") @SerializedName("pic_not_found") val picNotFoundList : String?,
    @ColumnInfo(name = "step_list") @SerializedName("step_list") val stepString : String?,
    @ColumnInfo(name = "add_time") @SerializedName("add_time") val addTime : String,
    @ColumnInfo(name = "update_time") @SerializedName("update_time") val updateTime : String,

    //@ColumnInfo(name = "command") val command : ArrayList<Command> = arrayListOf(),
){
    @Ignore
    var skipFlag : Boolean = false
    @Ignore
    val command : ArrayList<Command> = arrayListOf()
    @Ignore
    var picNeedFindList :List<String>? = null
    @Ignore
    var picNotNeedFindList :List<String>? = null
    @Ignore
    var stepList :List<Int>? = null
    @Ignore
    var point:Point?=null
}