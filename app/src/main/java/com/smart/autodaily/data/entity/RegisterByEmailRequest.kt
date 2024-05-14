package com.smart.autodaily.data.entity

data class RegisterByEmailRequest(
    val email: String,
    val emailCheckCode: String,
    val password: String,
    val inviteCodeFather: String
)
