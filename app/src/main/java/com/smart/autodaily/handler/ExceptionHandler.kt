package com.smart.autodaily.handler

import android.content.Context
import android.util.Log

class ExceptionHandler (private val context: Context) : Thread.UncaughtExceptionHandler {

    private val defaultHandler: Thread.UncaughtExceptionHandler? = Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        // 在这里处理异常，如记录异常信息到日志文件，展示对话框等
        Log.e("Exception Handler", "Unhandled exception: ", throwable)

        // 重新启动应用或执行其他操作
        /*val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)*/
        // 调用默认的异常处理器
        defaultHandler?.uncaughtException(thread, throwable)
    }
}