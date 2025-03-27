package com.smart.autodaily

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Process
import android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE
import com.jakewharton.threetenabp.AndroidThreeTen
import com.jeremyliao.liveeventbus.LiveEventBus
import com.smart.autodaily.config.AppConfig.channelIdDownload
import com.smart.autodaily.handler.RunScript
import com.smart.autodaily.utils.checkScriptUpdate
import com.smart.autodaily.utils.partScope
import com.smart.autodaily.utils.updateScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.weishu.reflection.Reflection
import splitties.systemservices.notificationManager

class App : Application() {
    companion object {
        private const val PREFS_NAME = "notification_prefs"
        internal const val KEY_NOTIFICATION_PERMISSION_CHECKED = "notification_permission_checked"
        internal lateinit var sharedPreferences: SharedPreferences
    }

    override fun onCreate() {
        super.onCreate()
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        
        //初始化时区
        AndroidThreeTen.init(this)
        //Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler(this))
        createNotificationChannels()
        LiveEventBus.config()
            .lifecycleObserverAlwaysActive(true)
            .autoClear(false)
            .enableLogger(true)
            //.enableLogger(BuildConfig.DEBUG)
            //.setLogger(EventLogger())
        Process.setThreadPriority(THREAD_PRIORITY_MORE_FAVORABLE)
        
        partScope.launch {
            //初始化全局设置
            RunScript.initGlobalSet()
            try {
                // 延迟检查更新
                delay(1000)
                checkScriptUpdate()
            } catch (e: Exception) {
                updateScope.coroutineContext.cancelChildren()
            }
        }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        //反射开启
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            Reflection.unseal(base)
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val downloadChannel = NotificationChannel(
            channelIdDownload,
            getString(R.string.action_download),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            enableLights(false)
            enableVibration(false)
            setSound(null, null)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        //向notification manager 提交channel
        notificationManager.createNotificationChannels(
            listOf(
                downloadChannel,
            )
        )
    }
}