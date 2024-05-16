package com.smart.autodaily.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import com.google.gson.annotations.SerializedName

@Entity(tableName = "user_info",
    primaryKeys = ["email"],
    indices = [Index(value = ["email"], unique = false),Index(value = ["user_id"], unique = false)])
data class UserInfo (
    @ColumnInfo(name = "user_id")   @SerializedName("user_id")  val userId: Int,
    @ColumnInfo(name = "phone")    @SerializedName("phone")  val phone: String?,
    @ColumnInfo(name = "email")   @SerializedName("email")   val email: String,
    @ColumnInfo(name = "password")   @SerializedName("password")   var password: String,
    @ColumnInfo(name = "invite_code")    @SerializedName("invite_code")  val inviteCode: String,
    @ColumnInfo(name = "invite_code_father")   @SerializedName("invite_code_father")   val inviteCodeFather: String?,
    @ColumnInfo(name = "key_type_id")   @SerializedName("key_type_id")   val keyTypeId: Byte,
    @ColumnInfo(name = "can_activate_num")   @SerializedName("can_activate_num")   var canActivateNum: Byte,
    @ColumnInfo(name = "register_time")    @SerializedName("register_time")  val registerTime: String,
    @ColumnInfo(name = "isLogin") var isLogin: Boolean?
)