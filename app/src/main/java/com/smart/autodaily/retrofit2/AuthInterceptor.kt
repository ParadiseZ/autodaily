package com.smart.autodaily.retrofit2

import com.smart.autodaily.utils.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {
    // 不需要登录验证的API路径白名单
    private val whiteList = listOf(
        "/loginByEmail",
        "/registerByEmail",
        "/sendEmailCode",
        "/resetPwdByEmail"
    )

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestPath = originalRequest.url.encodedPath
        
        // 检查是否是白名单路径
        if (whiteList.any { requestPath.contains(it) }) {
            return chain.proceed(originalRequest)
        }

        // 非白名单路径需要验证token
        val token = TokenManager.getToken()
        if (token == null) {
            // 如果没有token，跳转到登录页面
            /*val intent = Intent("android.intent.action.LOGIN")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            appCtx.startActivity(intent)*/
            throw IllegalStateException("未登录")
        }
        
        val newRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        val response = chain.proceed(newRequest)
        
        // 处理响应头中的新token
        response.header("New-Token")?.let { newToken ->
            TokenManager.saveToken(newToken)
        }
        
        return response
    }
} 