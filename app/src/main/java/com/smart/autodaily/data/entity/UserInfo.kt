package com.smart.autodaily.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

@Entity(tableName = "user_info",
    primaryKeys = ["email"],
    indices = [Index(value = ["email"], unique = true),Index(value = ["user_id"], unique = true)])
data class UserInfo (
    @ColumnInfo(name = "user_id") val userId: Int,
    @ColumnInfo(name = "user_name") var userName: String,
    @ColumnInfo(name = "password") var password: String,
    @ColumnInfo(name = "email") val email: String,
    @ColumnInfo(name = "phone") val phone: String,
    @ColumnInfo(name = "invitation_code") val invitationCode: String,
    @ColumnInfo(name = "key_type_id") val keyTypeId: Byte,
    @ColumnInfo(name = "can_activate_num") var canActivateNum: Byte,
    @ColumnInfo(name = "isLogin") var isLogin: Boolean=true
)