package com.smart.autodaily.utils

import android.content.Context
import android.content.Intent
import com.smart.autodaily.constant.RunButtonClickResult
import splitties.init.appCtx

fun checkLoginRes(context: Context = appCtx, rbcr : RunButtonClickResult){
    if (rbcr == RunButtonClickResult.NOT_LOGIN)
        context.startActivity(Intent("android.intent.action.LOGIN").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}