package com.smart.autodaily.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.os.Parcelable
import androidx.core.app.NotificationCompat
import com.smart.autodaily.R
import com.smart.autodaily.constant.ForegroundServiceId
import com.smart.autodaily.utils.ScreenCaptureUtil
import com.smart.autodaily.viewmodel.mediaProjectionServiceStartFlag


class MediaProjectionService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        // 创建通知，Android 8.0+ 必须使用Notification Channel
        createNotificationChannel()
        //点击停止的服务
        val stopIntent = Intent(this, MediaProjectionService::class.java).apply {
            action = "STOP_FOREGROUND_SERVICE"
        }
        val stopPendingIntent =PendingIntent.getService(this, 0, stopIntent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE )

        // 建立通知并将服务置于前台
        val notification: Notification = NotificationCompat.Builder(this, ForegroundServiceId.MEDIA_PROJECTION)
            .setContentTitle("AutoDaily")
            .setContentText("屏幕录制正在运行")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .addAction(R.drawable.ic_launcher_background, "停止服务", stopPendingIntent)
            .build()
        startForeground(ForegroundServiceId.MEDIA_PROJECTION_SERVICE_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP_FOREGROUND_SERVICE") {
            applicationContext.stopService(Intent(applicationContext, MediaProjectionService::class.java))
        }
        //return super.onStartCommand(intent, flags, startId)
        try {
            if (ScreenCaptureUtil.mps == null && intent!= null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {//安卓13以上
                    ScreenCaptureUtil.mps = (intent.getParcelableExtra("data",Intent::class.java))?.let {
                        (ScreenCaptureUtil.mediaProjectionDataMap["mediaProjectionManager"] as MediaProjectionManager)
                            .getMediaProjection(intent.getIntExtra("resultCode", -1), it)
                    }
                } else {
                    val p: Parcelable? = intent.getParcelableExtra("data")
                    p?.let {
                        if (p is Intent) {
                            ScreenCaptureUtil.mps = (ScreenCaptureUtil.mediaProjectionDataMap["mediaProjectionManager"] as MediaProjectionManager)
                                .getMediaProjection(intent.getIntExtra("resultCode", -1), it as Intent)
                        }

                    }
                }
            }
        }catch (e: Exception) {
            println("MediaProjectionService onStartCommand getMediaProjection error: " + e.message)
        }
        mediaProjectionServiceStartFlag.value = true
        return START_STICKY;
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = "MediaProjection Service"
            val description = "AutoDaily前台服务"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(ForegroundServiceId.MEDIA_PROJECTION, name, importance)
            channel.description = description
            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    // 销毁时释放资源
    override fun onDestroy() {
        super.onDestroy()
        ScreenCaptureUtil.release()
        mediaProjectionServiceStartFlag.value = false
        println("停止MediaProjectionService")
    }
}