package com.smart.autodaily.utils
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
        (h / 2f).toInt().toShort(),
        (s * 255).toInt().toShort(),
        (v * 255).toInt().toShort()
    )
}