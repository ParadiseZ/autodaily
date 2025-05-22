package com.smart.autodaily.feature.scripting.domain.service

import com.smart.autodaily.data.entity.ScriptInfo
import com.smart.autodaily.feature.scripting.domain.model.ScriptRunConfig
import com.smart.autodaily.feature.scripting.domain.state.ScriptExecutionState
import kotlinx.coroutines.flow.StateFlow

interface ScriptExecutorService {
    suspend fun startExecution(
        script: ScriptInfo,
        initialConfig: ScriptRunConfig
    )

    suspend fun stopExecution(scriptId: Int)

    fun getExecutionStateFlow(scriptId: Int): StateFlow<ScriptExecutionState>
}
