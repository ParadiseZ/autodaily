package com.smart.autodaily.data.entity

data class OcrResult(
    val label : Set<Short>,
    val x : Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val xCenter: Float,
    val yCenter: Float
)