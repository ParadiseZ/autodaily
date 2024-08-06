package com.smart.autodaily

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.jeremyliao.liveeventbus.LiveEventBus
import com.smart.autodaily.config.AppConfig.channelIdDownload
import com.smart.autodaily.data.AppDb
import com.smart.autodaily.handler.ExceptionHandler
import com.smart.autodaily.utils.ServiceUtil
import com.smart.autodaily.utils.UpdateUtil
import com.smart.autodaily.utils.toastOnUi
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.weishu.reflection.Reflection
import org.opencv.android.OpenCVLoader
import splitties.init.appCtx
import splitties.systemservices.notificationManager


class App : Application(){
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler(this))
        LiveEventBus.config()
            .lifecycleObserverAlwaysActive(true)
            .autoClear(false)
            .enableLogger(true)
            //.enableLogger(BuildConfig.DEBUG)
            //.setLogger(EventLogger())
        GlobalScope.launch(Dispatchers.IO){
            AppDb.getInstance(applicationContext)
            loadOpenCVLibraries();
            UpdateUtil.checkScriptUpdate()
            ServiceUtil.runUserService(appCtx)
        }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        //反射开启
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
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

    private fun loadOpenCVLibraries() {
        if (!OpenCVLoader.initLocal()) {
            applicationContext.toastOnUi("init OpenCVLibraries Failed")
            println("init OpenCVLibraries Failed")
            //Log.e(TAG, "OpenCV initialization failed")
        }
    }
}