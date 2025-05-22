package com.smart.autodaily.feature.scripting.domain.model

data class ScriptRunConfig(
    val intervalTime: Long,
    val similarScore: Float,
    val pkgName: String,
    val rebootDelay: Long,
    val tryBackAction: Boolean,
    val useGpu: Boolean,
    val imgSize: Int,
    // Add other configuration parameters as needed based on `conf` from RunScript.kt
    val cpuThreadNum: Int = 2, // Example default value
    val cpuPower: Int = 0, // Example default value
    val enableNHWC: Boolean = true, // Example default value
    val enableDebug: Boolean = false // Example default value
)
