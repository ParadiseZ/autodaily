package com.smart.autodaily.data.entity.request

import com.smart.autodaily.data.entity.UserInfo

data class Request<T>(
    var user : UserInfo? =null,
    var data: T ?= null
)
