package com.smart.autodaily.data.entity

import com.google.gson.annotations.SerializedName

data class VirtualCoinRecord(
    @SerializedName("user_id") val userId : Int,
    @SerializedName("change_type") val changeType : String,
    @SerializedName("add_time") val addTime : Int,
    @SerializedName("get_num") val changeValue : String
)
