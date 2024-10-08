package com.smart.autodaily.utils

import java.util.Calendar

fun isBetweenHour(start : Int, end : Int): Boolean {
    return Calendar.getInstance().get(Calendar.HOUR_OF_DAY) in start..end
}