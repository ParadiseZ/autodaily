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
    //类型名称
    @ColumnInfo(name = "key_type_name")   @SerializedName("key_type_name")   var keyTypeName: String?,
    //可激活个数
    @ColumnInfo(name = "can_activate_num")   @SerializedName("can_activate_num")   var canActivateNum: Byte?,
    //过期时间
    @ColumnInfo(name = "expiration_time")   @SerializedName("expiration_time")   var expirationTime: String?,
    //注册时间
    @ColumnInfo(name = "register_time")    @SerializedName("register_time")  val registerTime: String,
    //是否登录
    @ColumnInfo(name = "isLogin") var isLogin: Boolean?
)