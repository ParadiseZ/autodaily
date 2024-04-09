package com.smart.autodaily

import android.app.Application
import com.smart.autodaily.data.AppDb
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application(){
    override fun onCreate() {
        super.onCreate()
        AppDb.getInstance(applicationContext)
    }
}