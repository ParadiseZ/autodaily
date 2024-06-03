package com.smart.autodaily

import android.app.Application
import android.content.Context
import com.smart.autodaily.data.AppDb
import com.smart.autodaily.handler.ExceptionHandler
import com.smart.autodaily.utils.UpdateUtil
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class App : Application(){
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler(this))
        AppDb.getInstance(applicationContext)
        GlobalScope.launch(Dispatchers.IO){
            UpdateUtil.checkScriptUpdate()
        }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        //反射开启
    }
}