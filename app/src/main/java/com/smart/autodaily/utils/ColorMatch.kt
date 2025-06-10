package com.smart.autodaily.utils

import kotlin.math.roundToInt

fun rgbToHsv(rgb: List<Short>): List<Short> {
    val r = rgb[0].toFloat() / 255f
    val g = rgb[1].toFloat() / 255f
    val b = rgb[2].toFloat() / 255f

    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val delta = max - min

    var h = 0f
    var s = 0f
    val v = max

    if (delta != 0f) {
        s = delta / max
        when (max) {
            r -> h = (g - b) / delta
            g -> h = (b - r) / delta + 2f
            b -> h = (r - g) / delta + 4f
        }
        h*= 60f
        if (h < 0) {
            h += 360f
        }
    }
    return listOf(
        (h / 2f).roundToInt().toShort(),    // H: 0-360 → 0-180
        (s * 255).roundToInt().toShort(),   // S: 0.0-1.0 → 0-255
        (v * 255).roundToInt().toShort()     // V: 0.0-1.0 → 0-255
    )
}