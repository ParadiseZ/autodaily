package com.smart.autodaily.data.entity
data class DetectResult(
    val label: Int,
    val prob: Float,
    val rect: Rect,
    val xCenter: Float,
    val yCenter: Float
)