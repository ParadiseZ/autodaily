package com.smart.autodaily.utils

import com.smart.autodaily.BuildConfig

fun debug(msg: String){
    if(BuildConfig.DEBUG){
        println(msg)
    }
}