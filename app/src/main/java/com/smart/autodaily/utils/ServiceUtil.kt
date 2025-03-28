package com.smart.autodaily.utils

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Context.ACCESSIBILITY_SERVICE
import android.view.accessibility.AccessibilityManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku

object ServiceUtil {
    //服务连接时发送、
    val serviceBoundChannel = Channel<Unit>(1)
    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val am = context.getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager
        val accessibilityServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        return accessibilityServices.any {
            it.id.replace("/", "") == it.resolveInfo.serviceInfo.name
        }
    }

    suspend fun waitShizukuService(){
        serviceBoundChannel.receive()
    }
    suspend fun runUserService(context: Context){
        try {
            ShizukuUtil.requestShizukuPermission(context)
            for (i in 1 .. 10){
                if(ShizukuUtil.grant){
                    if(ShizukuUtil.iUserService == null){
                        binderScope.launch {
                            Shizuku.bindUserService(
                                ShizukuUtil.userServiceArgs,
                                ShizukuUtil.serviceConnection
                            )
                        }
                    }else{
                        serviceBoundChannel.send(Unit)
                    }
                    return
                }
                delay(1000)
            }
            serviceBoundChannel.send(Unit)
            SnackbarUtil.show("等待授权超时！")
        }catch (e : Exception){
            serviceBoundChannel.send(Unit)
        }
    }
}