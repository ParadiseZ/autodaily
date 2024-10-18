package com.smart.autodaily.handler

import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.cancelChildren

val globalCEHandler = CoroutineExceptionHandler { context, exception ->
    Log.e("${context[CoroutineName]}", "$exception，${exception.message}")
    context.cancelChildren()
    /*if (context[CoroutineName]?.name == "runScope" || context[CoroutineName]?.name=="binderScope"){

    }*/
}