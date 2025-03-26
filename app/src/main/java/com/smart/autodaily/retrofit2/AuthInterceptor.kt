package com.smart.autodaily.retrofit2

import com.smart.autodaily.utils.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = TokenManager.getToken()
        
        val newRequest = if (token != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }
        newRequest.header("New-Token")?.let { newToken ->
            // 更新本地存储的token
            TokenManager.saveToken(newToken.toString())
        }
        return chain.proceed(newRequest)
    }
} 