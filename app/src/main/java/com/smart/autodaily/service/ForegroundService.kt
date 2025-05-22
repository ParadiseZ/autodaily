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
import com.smart.autodaily.data.AppDb // For direct DAO access if needed for ScriptInfo
import com.smart.autodaily.feature.scripting.domain.repository.ScriptConfigRepository
import com.smart.autodaily.feature.scripting.domain.usecase.InitializeGlobalScriptConfigUseCase
import com.smart.autodaily.feature.scripting.domain.usecase.StartScriptUseCase
// import com.smart.autodaily.handler.RunScript // Replaced
import com.smart.autodaily.utils.Lom
import com.smart.autodaily.utils.runScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint // Assuming Hilt for DI
class ForegroundService : Service() {
    @Inject lateinit var startScriptUseCase: StartScriptUseCase
    @Inject lateinit var initializeGlobalScriptConfigUseCase: InitializeGlobalScriptConfigUseCase
    @Inject lateinit var scriptConfigRepository: ScriptConfigRepository // To fetch the default script

    companion object {
        private const val CHANNEL_ID = "ForegroundServiceChannel"
        private const val DEFAULT_SCRIPT_ID_TO_RUN = 1 // Example default script ID
        private const val NOTIFICATION_ID = 1
        const val ACTION_RUN_SCRIPT = "com.smart.autodaily.RUN_SCRIPT"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_RUN_SCRIPT -> {
                Lom.i("ForegroundService", "ACTION_RUN_SCRIPT received.")
                runScope.launch {
                    try {
                        initializeGlobalScriptConfigUseCase.execute()
                        
                        // Fetch the default script to run
                        // Note: scriptConfigRepository.getScriptInfoById() might not exist,
                        // using appDb.scriptInfoDao directly for now as per existing patterns.
                        // Ideally, repository would provide this.
                        val scriptToRun = appDb.scriptInfoDao.getScriptInfoByScriptId(DEFAULT_SCRIPT_ID_TO_RUN)
                        
                        if (scriptToRun != null) {
                            Lom.i("ForegroundService", "Attempting to start script ID: ${scriptToRun.scriptId} - ${scriptToRun.scriptName}")
                            startScriptUseCase.execute(scriptToRun)
                        } else {
                            Lom.e("ForegroundService", "Default script with ID $DEFAULT_SCRIPT_ID_TO_RUN not found.")
                        }
                    } catch (e: Exception) {
                        Lom.e("ForegroundService", "Error starting script from service: ${e.message}", e)
                    }
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