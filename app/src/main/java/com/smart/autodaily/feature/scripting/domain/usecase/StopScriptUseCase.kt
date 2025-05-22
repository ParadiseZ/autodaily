package com.smart.autodaily.feature.scripting.domain.usecase

import com.smart.autodaily.feature.scripting.domain.service.ScriptExecutorService

class StopScriptUseCase(
    private val scriptExecutorService: ScriptExecutorService
) {
    suspend fun execute(scriptId: Int) {
        scriptExecutorService.stopExecution(scriptId)
    }
}
