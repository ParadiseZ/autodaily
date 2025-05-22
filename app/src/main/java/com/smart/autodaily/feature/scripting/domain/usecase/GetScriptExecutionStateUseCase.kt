package com.smart.autodaily.feature.scripting.domain.usecase

import com.smart.autodaily.feature.scripting.domain.state.ScriptExecutionState
import com.smart.autodaily.feature.scripting.domain.state.ScriptStateManager
import kotlinx.coroutines.flow.StateFlow

class GetScriptExecutionStateUseCase(
    private val scriptStateManager: ScriptStateManager
) {
    fun execute(scriptId: Int): StateFlow<ScriptExecutionState> {
        return scriptStateManager.getScriptExecutionState(scriptId)
    }
}
