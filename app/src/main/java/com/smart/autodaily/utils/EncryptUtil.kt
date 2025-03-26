package com.smart.autodaily.utils

import java.security.MessageDigest
import java.math.BigInteger

object EncryptUtil {
    /**
     * 使用SHA-256算法对字符串进行加密
     * @param input 需要加密的字符串
     * @return 加密后的字符串
     */
    fun encryptSHA256(input: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val messageDigest = md.digest(input.toByteArray())
        val no = BigInteger(1, messageDigest)
        var hashText = no.toString(16)
        
        // 补齐64位
        while (hashText.length < 64) {
            hashText = "0$hashText"
        }
        
        return hashText
    }
}