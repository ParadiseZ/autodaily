package com.smart.autodaily.feature.scripting.domain.state

import kotlinx.coroutines.flow.StateFlow

interface ScriptStateManager {
    val currentOverallState: StateFlow<Boolean> // true if any script is running

    fun getScriptExecutionState(scriptId: Int): StateFlow<ScriptExecutionState>

    suspend fun setScriptRunning(scriptId: Int, isRunning: Boolean)

    suspend fun updateScriptRunStatus(scriptId: Int, flowIdType: Int, status: Int) // For ScriptRunStatus table

    suspend fun updateScriptSetRunStatus(scriptId: Int, flowId: Int, flowIdType: Int, status: Int) // For ScriptSetRunStatus table

    suspend fun incrementRunCount(scriptId: Int)

    suspend fun resetRunCount(scriptId: Int)

    suspend fun shouldSkipFlow(flowId: Int): Boolean

    suspend fun addSkipFlow(flowId: Int)

    suspend fun removeSkipFlow(flowId: Int)

    suspend fun clearAllSkipFlows()

    suspend fun shouldSkipAction(actionId: Int): Boolean

    suspend fun addSkipAction(actionId: Int)

    suspend fun removeSkipAction(actionId: Int)

    suspend fun clearAllSkipActions()
}
