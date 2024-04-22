package com.smart.autodaily

import android.app.Application
import android.content.Context
import com.smart.autodaily.data.AppDb
import com.smart.autodaily.data.appDb
import com.smart.autodaily.utils.ScreenUtil
import me.weishu.reflection.Reflection

class App : Application(){
    override fun onCreate() {
        super.onCreate()
        AppDb.getInstance(applicationContext)
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        //反射开启
        Reflection.unseal(base)
    }
}