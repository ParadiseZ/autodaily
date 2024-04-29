package com.smart.autodaily

import android.app.Application
import android.content.Context
import com.smart.autodaily.data.AppDb

class App : Application(){
    override fun onCreate() {
        super.onCreate()
        AppDb.getInstance(applicationContext)
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        //反射开启
    }
}