package com.smart.autodaily.utils


import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Context.ACCESSIBILITY_SERVICE
import android.view.accessibility.AccessibilityManager
import rikka.shizuku.Shizuku

object ServiceUtil {
    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val am = context.getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager
        val accessibilityServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        return accessibilityServices.any {
            it.id.replace("/", "") == it.resolveInfo.serviceInfo.name
        }
    }

    fun runUserService(context: Context){
        try {
            ShizukuUtil.requestShizukuPermission()
            if(ShizukuUtil.grant){
                if(ShizukuUtil.iUserService == null){
                    context.toastOnUi("shell服务启动中...")
                    Shizuku.bindUserService(
                        ShizukuUtil.userServiceArgs,
                        ShizukuUtil.serviceConnection
                    )
                }else{
                    context.toastOnUi("shell服务已启动！")
                }
            }else{
                context.toastOnUi("shizuku未授权！")
            }
        }catch (e : Exception){
            context.toastOnUi("shizuku服务异常！")
        }
    }
}