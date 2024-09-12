package com.smart.autodaily.data.entity

import com.google.gson.annotations.SerializedName

data class UserKeyRecord(
    @SerializedName("user_id") val userId : Int,
    @SerializedName("add_time") val addTime : String,
    @SerializedName("expiration_time") val expirationTime : String,
    @SerializedName("vip_level") val vipLevel : String,
    @SerializedName("create_desc") val createDesc : String
)
