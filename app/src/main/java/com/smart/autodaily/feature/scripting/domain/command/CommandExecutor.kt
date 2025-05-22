package com.smart.autodaily.feature.scripting.domain.command

import com.smart.autodaily.data.entity.Point
import com.smart.autodaily.data.entity.Rect

interface CommandExecutor {
    suspend fun executeAdbClick(point: Point?): Boolean
    suspend fun executeAdbSwipe(rect: Rect?): Boolean
    suspend fun executeAdbBack(): Boolean
    suspend fun executeAdbRebootApp(packageName: String): Boolean
    suspend fun executeAdbStartApp(packageName: String): Boolean
    // Add other ADB-related commands as needed
}
