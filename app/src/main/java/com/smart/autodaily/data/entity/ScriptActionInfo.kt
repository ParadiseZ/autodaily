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
    @ColumnInfo(name = "script_id") @SerializedName("scriptId") val scriptId: Int,
    @ColumnInfo(name = "flow_id") @SerializedName("flowId") val flowId: Int,
    @ColumnInfo(name = "set_value") @SerializedName("setValue") val setValue: String?,
    @ColumnInfo(name = "action_string") @SerializedName("actionString") val actionString : String,
    @ColumnInfo(name = "execute_max") @SerializedName("executeMax") val executeMax : Int = 1,
    @ColumnInfo(name = "page_desc") @SerializedName("pageDesc") val pageDesc : String?,
    @ColumnInfo(name = "add_time") @SerializedName("addTime") val addTime : String,
    @ColumnInfo(name = "update_time") @SerializedName("updateTime") val updateTime : String?,
    @ColumnInfo(name = "oper_txt") @SerializedName("operTxt") val operTxt : Boolean,
    @ColumnInfo(name = "int_label") @SerializedName("intLabel") val intLabel : String?,
    @ColumnInfo(name = "int_exc_label") @SerializedName("intExcLabel") val intExcLabel : String?,
    @ColumnInfo(name = "txt_label") @SerializedName("txtLabel") val txtLabel : String?,
    @ColumnInfo(name = "txt_exc_label") @SerializedName("txtExcLabel") val txtExcLabel : String?,

    @ColumnInfo(name = "sort") @SerializedName("sort") val sort : Int
    //@ColumnInfo(name = "command") val command : ArrayList<Command> = arrayListOf(),
){
    @Ignore
    var skipFlag : Boolean = false
    @Ignore
    val command : ArrayList<Command> = arrayListOf()
    @Ignore
    var intLabelSet : Set<Short> = setOf()
    @Ignore
    var intExcLabelSet : Set<Short> = setOf()
    @Ignore
    var intFirstLab : Short = 0
    @Ignore
    var txtLabelSet : List<Set<Short>> = listOf()
    @Ignore
    var txtExcLabelSet : List<Set<Short>> = listOf()
    @Ignore
    var txtFirstLab : Set<Short> = setOf()
    @Ignore
    var point:Point?=null
    @Ignore
    var executeCur: Int= 0
    @Ignore
    var swipePoint : Rect?=null
    @Ignore
    var addFlag : Boolean = true
}