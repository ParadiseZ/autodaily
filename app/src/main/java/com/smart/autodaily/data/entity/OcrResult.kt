package com.smart.autodaily.data.entity

data class OcrResult(
    val label : Set<Short>,
    val x : Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val xCenter: Float,
    val yCenter: Float,
    var colorSet : Set<Short>,
    val colorArr : ShortArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OcrResult

        if (label != other.label) return false
        if (x != other.x) return false
        if (y != other.y) return false
        if (width != other.width) return false
        if (height != other.height) return false
        if (xCenter != other.xCenter) return false
        if (yCenter != other.yCenter) return false
        if (colorSet != other.colorSet) return false
        if (!colorArr.contentEquals(other.colorArr)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = label.hashCode()
        result = 31 * result + x.hashCode()
        result = 31 * result + y.hashCode()
        result = 31 * result + width.hashCode()
        result = 31 * result + height.hashCode()
        result = 31 * result + xCenter.hashCode()
        result = 31 * result + yCenter.hashCode()
        result = 31 * result + colorSet.hashCode()
        result = 31 * result + colorArr.contentHashCode()
        return result
    }
}