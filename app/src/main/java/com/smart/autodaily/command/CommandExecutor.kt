package com.smart.autodaily.command

import android.graphics.Bitmap

interface CommandExecutor {
    fun execCap(scale : Int) : Bitmap?
    fun execVoidCommand(command:String)
    fun exceptionHandler(e : Exception)
}