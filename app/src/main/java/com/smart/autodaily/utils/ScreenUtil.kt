package com.smart.autodaily.utils

import android.content.Context
import android.content.res.Resources
import android.widget.Toast
import androidx.compose.runtime.compositionLocalOf

object ScreenUtil {

    lateinit var context : Context

    fun setContextInstance(context: Context){
        this.context = context
    }
    //弹窗
    fun showMsg (message:String, context: Context){
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}