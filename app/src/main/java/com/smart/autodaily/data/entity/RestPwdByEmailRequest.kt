package com.smart.autodaily.data.entity

data class RestPwdByEmailRequest (
    val email: String,
    val emailCheckCode: String,
    val password: String,
)