package com.smart.autodaily.utils


import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Context.ACCESSIBILITY_SERVICE
import android.view.accessibility.AccessibilityManager
import kotlinx.coroutines.delay
import rikka.shizuku.Shizuku

object ServiceUtil {
    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val am = context.getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager
        val accessibilityServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        return accessibilityServices.any {
            it.id.replace("/", "") == it.resolveInfo.serviceInfo.name
        }
    }

    suspend fun runUserService(context: Context){
        try {
            ShizukuUtil.requestShizukuPermission(context)
            for (i in 1 .. 10){
                if(ShizukuUtil.grant){
                    if(ShizukuUtil.iUserService == null){
                        Shizuku.bindUserService(
                            ShizukuUtil.userServiceArgs,
                            ShizukuUtil.serviceConnection
                        )
                    }
                    return
                }
                delay(1000)
            }
        }catch (e : Exception){
            context.toastOnUi("shizuku服务异常！")
        }
    }
}