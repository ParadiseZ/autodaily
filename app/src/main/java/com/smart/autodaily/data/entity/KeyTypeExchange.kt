package com.smart.autodaily.data.entity

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class KeyTypeExchange(
    @SerializedName("id") val id : Int,
    @SerializedName("type_name") val typeName : String,
    @SerializedName("type_desc") val typeDesc : String,
    @SerializedName("can_run_num") val canRunNum : Int,
    @SerializedName("vip_level") val vipLevel : Int,
    @SerializedName("price") val price : BigDecimal,
    @SerializedName("buy_price") val buyPrice : BigDecimal
)
