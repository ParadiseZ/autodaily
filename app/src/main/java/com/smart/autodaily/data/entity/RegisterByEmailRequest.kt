package com.smart.autodaily.data.entity

data class RegisterByEmailRequest(
    val email: String,
    val password: String,
    val inviteCode: String
)
