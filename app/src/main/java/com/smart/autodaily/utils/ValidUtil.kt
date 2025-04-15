package com.smart.autodaily.utils

object ValidUtil {
    fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
        return email.matches(emailRegex.toRegex())
    }

    fun isValidPassword(password: String): Boolean {
        val regex = "^\\S{8,}\$"
        //val regex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$"
        return password.matches(regex.toRegex())
    }
    fun isValidEmailCode(str: String): Boolean {
        val regex = "^\\S{6}\$"
        return str.matches(regex.toRegex())
    }
    fun isNumeric(str: String): Boolean {
        return str.matches("\\d+".toRegex())
    }
}