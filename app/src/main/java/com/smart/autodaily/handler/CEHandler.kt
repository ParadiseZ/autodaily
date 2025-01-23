package com.smart.autodaily.handler

import android.util.Log
import com.smart.autodaily.utils.Lom
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren

val globalCEHandler = CoroutineExceptionHandler { context, exception ->
    val name = context[CoroutineName]?.name
    when(name){
        "logScope" -> {
            Lom.n("$exception", "${exception.message}")
            exception.printStackTrace()
        }
        "runScope" -> {
            isRunning.intValue = 0
        }
        else -> Log.e("${context[CoroutineName]}", "$exception，${exception.message}")
    }
    (context[Job] ?: return@CoroutineExceptionHandler).cancelChildren()
}