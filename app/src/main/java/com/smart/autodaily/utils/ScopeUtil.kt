package com.smart.autodaily.utils

import com.smart.autodaily.handler.globalCEHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren

//更新
val updateScope = CoroutineScope(Dispatchers.IO + globalCEHandler + CoroutineName("updateScope") + SupervisorJob())
//启动app
val partScope = CoroutineScope(Dispatchers.IO + globalCEHandler + CoroutineName("partScope") + SupervisorJob())
//运行
val runScope = CoroutineScope(Dispatchers.IO +globalCEHandler + CoroutineName("runScope") + SupervisorJob())
//shizuku binder
val binderScope = CoroutineScope(Dispatchers.IO +globalCEHandler + CoroutineName("binderScope") + SupervisorJob())
fun cancelChildrenJob(){
    updateScope.coroutineContext.cancelChildren()
    partScope.coroutineContext.cancelChildren()
    runScope.coroutineContext.cancelChildren()
    binderScope.coroutineContext.cancelChildren()
}

fun cancelJob(){
    updateScope.cancel()
    partScope.cancel()
    runScope.cancel()
    binderScope.cancel()
}