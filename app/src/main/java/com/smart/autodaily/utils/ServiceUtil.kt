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
            ShizukuUtil.requestShizukuPermission(context)
            for (i in 1 .. 10){
                if(ShizukuUtil.grant){
                    if(ShizukuUtil.iUserService == null){
                        Shizuku.bindUserService(
                            ShizukuUtil.userServiceArgs,
                            ShizukuUtil.serviceConnection
                        )
                        //context.toastOnUi("shell服务启动成功！")
                        return
                    }
                }
            }
        }catch (e : Exception){
            context.toastOnUi("shizuku服务异常！")
        }
    }
}