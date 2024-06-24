package com.smart.autodaily.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import com.google.gson.annotations.SerializedName

@Entity(tableName = "script_action_info", primaryKeys = ["id"], indices = [Index(value = ["script_id", "set_id","set_value", "pic_id"])])
data class ScriptActionInfo(
    @ColumnInfo(name = "id") @SerializedName("id") val id: Int,
    @ColumnInfo(name = "script_id") @SerializedName("script_id") val scriptId: Int,
    @ColumnInfo(name = "set_id") @SerializedName("set_id") val setId: Int,
    @ColumnInfo(name = "set_value") @SerializedName("set_value") var setValue: String,
    @ColumnInfo(name = "pic_id") @SerializedName("pic_id") val picId : Int,
    @ColumnInfo(name = "action_string") @SerializedName("action_string") var actionString : String,
    //script_info
    var picPath : String = "",
    //pic_info
    var picName : String = "",

    var point: Point?= null,
    val command : ArrayList<ScriptActionInfo.() -> ScriptActionInfo>
)
