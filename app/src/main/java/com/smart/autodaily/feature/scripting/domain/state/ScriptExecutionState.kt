package com.smart.autodaily.feature.scripting.domain.state

sealed class ScriptExecutionState {
    object Idle : ScriptExecutionState()
    object LoadingModel : ScriptExecutionState()
    data class Running(val currentAction: String) : ScriptExecutionState()
    object Paused : ScriptExecutionState()
    data class Error(val message: String) : ScriptExecutionState()
    data class Finished(val reason: String) : ScriptExecutionState()
}
