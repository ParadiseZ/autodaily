package com.smart.autodaily.feature.scripting.domain.usecase

import com.smart.autodaily.feature.scripting.domain.state.ScriptStateManager
import kotlinx.coroutines.flow.StateFlow

class GetOverallScriptingStateUseCase(
    private val scriptStateManager: ScriptStateManager
) {
    fun execute(): StateFlow<Boolean> {
        return scriptStateManager.currentOverallState
    }
}
