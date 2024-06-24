package com.smart.autodaily.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.google.gson.annotations.SerializedName

@Entity(tableName = "pic_info", primaryKeys = ["id"])
data class PicInfo(
    @ColumnInfo(name = "id") @SerializedName("id") val id: Int,
    @ColumnInfo(name = "pic_path") @SerializedName("pic_path") var picPath: String,
    @ColumnInfo(name = "pic_desc") @SerializedName("pic_desc") var picDesc: String
)
