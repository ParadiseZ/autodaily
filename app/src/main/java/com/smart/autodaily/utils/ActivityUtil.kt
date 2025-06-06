package com.smart.autodaily.utils

import android.content.Context
import android.content.Intent
import com.smart.autodaily.data.entity.UserInfo
import splitties.init.appCtx

fun isLogin(context: Context = appCtx, user: UserInfo?) : Boolean {
    if (user==null) {
        return false
    }
    return true
}

fun gotoExchange(context: Context = appCtx){
    context.startActivity(
        Intent("android.intent.action.COIN EXCHANGE").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}

fun gotoCoinDetail(context: Context = appCtx){
    context.startActivity(
        Intent("android.intent.action.COIN DETAIL").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}

fun gotoUserKeyRecord(context: Context = appCtx){
    context.startActivity(
        Intent("android.intent.action.KEY RECORD").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}