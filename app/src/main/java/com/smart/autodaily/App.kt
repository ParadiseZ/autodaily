package com.smart.autodaily

import android.app.Application
import com.smart.autodaily.data.AppDb

class App : Application(){
    override fun onCreate() {
        super.onCreate()
        //println(applicationContext.packageName+"=====")
        //AppDb.getInstance(applicationContext)
        AppDb.getInstance(applicationContext)
    }
}