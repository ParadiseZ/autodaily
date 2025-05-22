package com.smart.autodaily.feature.scripting.domain.service

import android.graphics.Bitmap
import com.smart.autodaily.data.entity.DetectResult

interface ModelDetectionService {
    suspend fun loadModel(
        modelPath: String,
        paramPath: String,
        binPath: String,
        imgSize: Int,
        useGpu: Boolean,
        cpuThreadNum: Int,
        cpuPower: Int,
        enableNHWC: Boolean,
        enableDebug: Boolean
    ): Boolean

    suspend fun detectObjects(
        bitmap: Bitmap,
        classesNum: Int
    ): List<DetectResult>
}
