package com.smart.autodaily.data.entity.resp

import com.smart.autodaily.data.entity.ScriptInfo

data class ScriptNetSearchResponse<T>(
    val code: Int = 0,
    val message: String,
    val data: List<T> = emptyList()
)