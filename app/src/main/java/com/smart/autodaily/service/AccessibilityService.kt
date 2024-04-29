package com.smart.autodaily.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.view.accessibility.AccessibilityEvent

class AccessibilityService : AccessibilityService() {
    override fun onServiceConnected() {
        super.onServiceConnected()
        println("AccessibilityService onServiceConnected")
        val accessibilityServiceInfo = AccessibilityServiceInfo()
        accessibilityServiceInfo.eventTypes = (/*AccessibilityEvent.TYPE_WINDOWS_CHANGED
                or AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                or AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED

                or*/ AccessibilityEvent.TYPE_VIEW_CLICKED// 点击事件
                or AccessibilityEvent.TYPE_VIEW_LONG_CLICKED// 长按事件
                or AccessibilityEvent.TYPE_GESTURE_DETECTION_START// 手势开始
                or AccessibilityEvent.TYPE_GESTURE_DETECTION_END// 手势结束
                )
        //accessibilityServiceInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK
        accessibilityServiceInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_HAPTIC
        accessibilityServiceInfo.notificationTimeout = 100 // 通知超时时间
        accessibilityServiceInfo.flags = AccessibilityServiceInfo.DEFAULT
        // 如果这里指定了包名则只会收到对应包名应用的事件
        //accessibilityServiceInfo.packageNames = arrayOf("com.yanggui.animatortest")
        serviceInfo = accessibilityServiceInfo
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let {
            if (it.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED){
                println("AccessibilityService onAccessibilityEvent click")
            }
        }
    }

    override fun onInterrupt() {
        println("AccessibilityService onInterrupt")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //return super.onStartCommand(intent, flags, startId)
        return START_STICKY //内存资源足够时再恢复
    }
    override fun onDestroy() {
        super.onDestroy()
        println("AccessibilityService onDestroy")
    }


    fun clickPoint(){
        performGlobalAction(GLOBAL_ACTION_BACK)
    }
}