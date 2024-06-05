package com.smart.autodaily.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.smart.autodaily.R
import com.smart.autodaily.constant.ForegroundServiceId
import com.smart.autodaily.utils.ScreenCaptureUtil
import com.smart.autodaily.viewmodel.mediaProjectionServiceStartFlag


class AppService: Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        println("appServiceOnCreate")
        // 创建通知，Android 8.0+ 必须使用Notification Channel
        createNotificationChannel()
        //点击停止的服务
        val stopIntent = Intent(this, MediaProjectionService::class.java).apply {
            action = "STOP_FOREGROUND_SERVICE"
        }
        val stopPendingIntent =PendingIntent.getService(this, 0, stopIntent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE )

        // 建立通知并将服务置于前台
        val notification: Notification = NotificationCompat.Builder(this, ForegroundServiceId.MEDIA_PROJECTION)
            .setContentTitle("AutoDaily正在运行")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .addAction(R.drawable.ic_launcher_background, "停止程序", stopPendingIntent)
            .build()
        startForeground(2, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = "AutoDaily"
            val description = "AutoDaily前台服务"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(ForegroundServiceId.APP_FOREGROUND_SERVICE_ID.toString(), name, importance)
            channel.description = description
            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("appServiceStartCommand")
        return START_NOT_STICKY
    }
    private fun stopService(){
        stopForeground(STOP_FOREGROUND_REMOVE)
        onDestroy()
    }

    override fun onDestroy() {
        super.onDestroy()
        ScreenCaptureUtil.release()
        mediaProjectionServiceStartFlag.value = false
        println("停止App")
    }
}