package com.smart.autodaily

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.smart.autodaily.data.AppDb
import com.smart.autodaily.handler.ExceptionHandler
import com.smart.autodaily.viewmodel.ApplicationViewModel

class App : Application(){
    val viewModel: ApplicationViewModel by lazy {
        ViewModelProvider.AndroidViewModelFactory.getInstance(this).create(ApplicationViewModel::class.java)
    }
    override fun onCreate() {
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler(this))
        AppDb.getInstance(applicationContext)
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        //反射开启
    }
}