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
    @ColumnInfo(name = "pic_id") @SerializedName("pic_id") val picId : Int,
    @ColumnInfo(name = "page_labels") @SerializedName("page_labels") val pageLabels : String,
    @ColumnInfo(name = "click_label_idx") @SerializedName("click_label_idx") val clickLabelIdx : Int?,
    //-1为第一个
    @ColumnInfo(name = "click_label_position") @SerializedName("click_label_idx") val clickLabelPosition : Int = -1,
    @ColumnInfo(name = "action_string") @SerializedName("action_string") var actionString : String,

    //action_string
    @ColumnInfo(name = "add_time") @SerializedName("add_time") val addTime : String,
    @ColumnInfo(name = "update_time") @SerializedName("update_time") val updateTime : String,

    //@ColumnInfo(name = "command") val command : ArrayList<Command> = arrayListOf(),
){
    @Ignore
    var skipFlag : Boolean = false
    @Ignore
    val command : ArrayList<Command> = arrayListOf()
    @Ignore
    var needFindSize : Int = -1
    @Ignore
    var labelSet : Set<Int> = hashSetOf()
    @Ignore
    var point:Point?=null
}