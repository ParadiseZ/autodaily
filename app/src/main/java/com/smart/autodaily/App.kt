package com.smart.autodaily

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.jakewharton.threetenabp.AndroidThreeTen
import com.jeremyliao.liveeventbus.LiveEventBus
import com.smart.autodaily.config.AppConfig.channelIdDownload
import com.smart.autodaily.data.AppDb
import com.smart.autodaily.handler.ExceptionHandler
import com.smart.autodaily.handler.RunScript
import com.smart.autodaily.utils.ServiceUtil
import com.smart.autodaily.utils.UpdateUtil
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.weishu.reflection.Reflection
import splitties.init.appCtx
import splitties.systemservices.notificationManager


class App : Application(){
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()
        //初始化时区
        AndroidThreeTen.init(this)
        Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler(this))
        LiveEventBus.config()
            .lifecycleObserverAlwaysActive(true)
            .autoClear(false)
            .enableLogger(true)
            //.enableLogger(BuildConfig.DEBUG)
            //.setLogger(EventLogger())
        AppDb.getInstance(applicationContext)
        GlobalScope.launch(Dispatchers.IO){
            //初始化全局设置
            RunScript.initGlobalSet()
            UpdateUtil.checkScriptUpdate()
            ServiceUtil.runUserService(appCtx)
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