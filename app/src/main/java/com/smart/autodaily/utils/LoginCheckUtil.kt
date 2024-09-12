package com.smart.autodaily.utils

import android.content.Context
import android.content.Intent
import com.smart.autodaily.data.entity.UserInfo
import splitties.init.appCtx

fun isLogin(context: Context = appCtx, user: UserInfo?) : Boolean {
    if (user==null) {
        context.startActivity(
            Intent("android.intent.action.LOGIN").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        return false
    }
    return true
}