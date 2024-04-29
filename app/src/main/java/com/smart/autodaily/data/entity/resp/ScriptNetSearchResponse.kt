package com.smart.autodaily.data.entity.resp

data class ScriptNetSearchResponse< T >(
    val code: Int = 0,
    val message: String,
    val data  :  T
)