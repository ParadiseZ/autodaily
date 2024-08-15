package com.smart.autodaily.data.entity

import android.graphics.RectF

data class DetectResult(
    val label: Int,
    val prob: Float,
    val rect: RectF
)