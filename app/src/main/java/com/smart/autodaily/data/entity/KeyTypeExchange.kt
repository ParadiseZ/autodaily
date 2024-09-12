package com.smart.autodaily.data.entity

import java.math.BigDecimal

data class KeyTypeExchange(
    val id : Int,
    val typeName : String,
    val typeDesc : String,
    val canRunNum : Int,
    val vipLevel : Int,
    val price : BigDecimal,
    val buyPrice : BigDecimal
)
