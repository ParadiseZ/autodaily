package com.smart.autodaily.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.smart.autodaily.R
import com.smart.autodaily.handler.INFO
import com.smart.autodaily.handler.RunScript
import com.smart.autodaily.handler.isRunning
import com.smart.autodaily.utils.Lom
import com.smart.autodaily.utils.RootUtil
import com.smart.autodaily.utils.runScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch

class ForegroundService : Service() {
    companion object {
        private const val CHANNEL_ID = "ForegroundServiceChannel"
        private const val NOTIFICATION_ID = 1
        const val ACTION_RUN_SCRIPT = "com.smart.autodaily.RUN_SCRIPT"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onDestroy() {
        super.onDestroy()
        RootUtil.close()
        runScope.coroutineContext.cancelChildren()
        isRunning.intValue=0
        Lom.n(INFO ,"前台服务被销毁，停止运行")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_RUN_SCRIPT -> {
                runScope.launch {
                    RunScript.initGlobalSet()
                    RunScript.runScript()
                }
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "前台服务通知",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "用于保持应用在前台运行的通知"
                enableLights(false)
                enableVibration(false)
                setSound(null, null)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AutoDaily")
            .setContentText("正在运行中")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setAutoCancel(false)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }
} 