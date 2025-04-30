package com.smart.autodaily.data.entity

data class OcrResult(
    var label : Set<Short>,
    val labelArr : ShortArray,
    val txt : String,
    var colorSet : Set<Int>,
    val colorArr : IntArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OcrResult

        if (label != other.label) return false
        if (!labelArr.contentEquals(other.labelArr)) return false
        if (txt != other.txt) return false
        if (colorSet != other.colorSet) return false
        if (!colorArr.contentEquals(other.colorArr)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = label.hashCode()
        result = 31 * result + labelArr.contentHashCode()
        result = 31 * result + txt.hashCode()
        result = 31 * result + colorSet.hashCode()
        result = 31 * result + colorArr.contentHashCode()
        return result
    }
}