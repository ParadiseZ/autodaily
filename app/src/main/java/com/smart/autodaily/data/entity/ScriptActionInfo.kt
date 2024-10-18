package com.smart.autodaily.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import com.google.gson.annotations.SerializedName
import com.smart.autodaily.command.Command

@Entity(tableName = "script_action_info", primaryKeys = ["id"], indices = [Index(value = ["script_id", "flow_id"])])
data class ScriptActionInfo(
    @ColumnInfo(name = "id") @SerializedName("id") val id: Int,
    @ColumnInfo(name = "script_id") @SerializedName("script_id") val scriptId: Int,
    @ColumnInfo(name = "flow_id") @SerializedName("flow_id") val flowId: Int,
    @ColumnInfo(name = "set_value") @SerializedName("set_value") val setValue: String?,
    @ColumnInfo(name = "page_labels") @SerializedName("page_labels") val pageLabels : String?,
    @ColumnInfo(name = "except_labels") @SerializedName("except_labels") val exceptLabels : String?,
    @ColumnInfo(name = "click_label_idx") @SerializedName("click_label_idx") val clickLabelIdx : Int =-1,
    @ColumnInfo(name = "click_label_position") @SerializedName("click_label_position") val clickLabelPosition : Int = -1,//-1为第一个
    @ColumnInfo(name = "action_string") @SerializedName("action_string") val actionString : String,
    @ColumnInfo(name = "execute_max") @SerializedName("execute_max") val executeMax : Int = 1,
    @ColumnInfo(name = "page_desc") @SerializedName("page_desc") val pageDesc : String?,
    //action_string
    @ColumnInfo(name = "add_time") @SerializedName("add_time") val addTime : String,
    @ColumnInfo(name = "update_time") @SerializedName("update_time") val updateTime : String?,

    @ColumnInfo(name = "only_labels") @SerializedName("only_labels") val onlyLabels : String?,
    @ColumnInfo(name = "sort") @SerializedName("sort") val sort : Int
    //@ColumnInfo(name = "command") val command : ArrayList<Command> = arrayListOf(),
){
    @Ignore
    var skipFlag : Boolean = false
    @Ignore
    val command : ArrayList<Command> = arrayListOf()
    @Ignore
    var pageLabelSet : Set<Int> = hashSetOf()
    @Ignore
    var exceptLabelSet : Set<Int> = hashSetOf()
    @Ignore
    var onlyLabelSet : Set<Int> = hashSetOf()
    @Ignore
    var point:Point?=null
    @Ignore
    var executeCur: Int= 0
}