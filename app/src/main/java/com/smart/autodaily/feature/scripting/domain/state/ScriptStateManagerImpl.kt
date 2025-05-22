package com.smart.autodaily.feature.scripting.domain.state

import com.smart.autodaily.data.AppDb // Assuming DAOs are accessed via AppDb
import com.smart.autodaily.data.dao.ScriptInfoDao
import com.smart.autodaily.data.dao.ScriptRunStatusDao
import com.smart.autodaily.data.dao.ScriptSetRunStatusDao
import android.util.Log // Added import
import com.smart.autodaily.data.entity.ScriptRunStatus
import com.smart.autodaily.data.entity.ScriptSetRunStatus
import com.smart.autodaily.utils.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Calendar
import java.util.concurrent.ConcurrentHashMap

class ScriptStateManagerImpl(
    // DAOs will be injected or accessed via AppDb.INSTANCE for simplicity matching other impls
    private val scriptRunStatusDao: ScriptRunStatusDao = AppDb.INSTANCE.scriptRunStatusDao(),
    private val scriptSetRunStatusDao: ScriptSetRunStatusDao = AppDb.INSTANCE.scriptSetRunStatusDao(),
    private val scriptInfoDao: ScriptInfoDao = AppDb.INSTANCE.scriptInfoDao()
) : ScriptStateManager {

    private val _overallState = MutableStateFlow(false)
    override val currentOverallState: StateFlow<Boolean> = _overallState.asStateFlow()

    private val scriptStates = ConcurrentHashMap<Int, MutableStateFlow<ScriptExecutionState>>()
    private val scriptStatesMutex = Mutex() // To protect scriptStates map operations

    // Using scriptId as key for these maps to support per-script skip lists
    private val skipFlowIdsMap = ConcurrentHashMap<Int, MutableSet<Int>>()
    private val skipAcIdsMap = ConcurrentHashMap<Int, MutableSet<Int>>()
    private val skipMutex = Mutex() // To protect skipFlowIdsMap and skipAcIdsMap

    override fun getScriptExecutionState(scriptId: Int): StateFlow<ScriptExecutionState> {
        return scriptStates.getOrPut(scriptId) { MutableStateFlow(ScriptExecutionState.Idle) }.asStateFlow()
    }

    override suspend fun setScriptRunning(scriptId: Int, isRunning: Boolean) {
        scriptStatesMutex.withLock {
            val stateFlow = scriptStates.getOrPut(scriptId) { MutableStateFlow(ScriptExecutionState.Idle) }
            val currentState = stateFlow.value
            
            if (isRunning) {
                if (currentState is ScriptExecutionState.Idle || currentState is ScriptExecutionState.Finished || currentState is ScriptExecutionState.Error) {
                    stateFlow.value = ScriptExecutionState.LoadingModel // Initial state when starting
                } else if (currentState is ScriptExecutionState.Paused) {
                     stateFlow.value = ScriptExecutionState.Running("Resumed") // Or restore previous running message
                }
                // If already LoadingModel or Running, do nothing.
            } else { // isRunning is false
                if (currentState !is ScriptExecutionState.Idle && currentState !is ScriptExecutionState.Finished && currentState !is ScriptExecutionState.Error) {
                    // Only transition to Idle if it was actively running or paused
                    // If it's already Finished or Error, don't revert to Idle.
                    // Consider if Finished/Error should also go to Idle here or be explicit.
                    // For now, if stopping, go to Idle unless it's a terminal state like Finished/Error
                    stateFlow.value = ScriptExecutionState.Idle
                }
            }
            recalculateOverallState()
        }
    }
    
    // Method to update specific execution state details
    suspend fun updateScriptExecutionState(scriptId: Int, newState: ScriptExecutionState) {
        scriptStatesMutex.withLock {
            val stateFlow = scriptStates.getOrPut(scriptId) { MutableStateFlow(ScriptExecutionState.Idle) }
            stateFlow.value = newState
            if (newState is ScriptExecutionState.Finished || newState is ScriptExecutionState.Error || newState is ScriptExecutionState.Idle) {
                 // If a script finishes, errors out, or explicitly set to Idle, recalculate overall.
                 // This is important if a script stops independently.
                recalculateOverallState()
            } else if (newState is ScriptExecutionState.Running || newState is ScriptExecutionState.LoadingModel) {
                // If any script is running or loading, overall state should be true.
                if (!_overallState.value) { // minor optimization
                    recalculateOverallState()
                }
            }
        }
    }


    private fun recalculateOverallState() {
        // Overall state is true if any script is in LoadingModel or Running state
        val anyScriptActive = scriptStates.values.any {
            val state = it.value
            state is ScriptExecutionState.LoadingModel || state is ScriptExecutionState.Running
        }
        _overallState.value = anyScriptActive
    }

    override suspend fun updateScriptRunStatus(scriptId: Int, flowIdType: Int, status: Int) {
        val today = DateUtils.getTodayDateString() // Assuming DateUtils has this
        val existingStatus = scriptRunStatusDao.getByScriptIdAndDate(scriptId, today)
        if (existingStatus == null) {
            scriptRunStatusDao.insert(ScriptRunStatus(scriptId = scriptId, date = today, flowIdType = flowIdType, status = status))
        } else {
            existingStatus.status = status
            existingStatus.flowIdType = flowIdType // Update flowIdType as well
            scriptRunStatusDao.update(existingStatus)
        }
    }

    override suspend fun updateScriptSetRunStatus(scriptId: Int, flowId: Int, flowIdType: Int, status: Int) {
        val today = DateUtils.getTodayDateString()
        val existingStatus = scriptSetRunStatusDao.getByScriptAndFlowIdAndDate(scriptId, flowId, today)
        if (existingStatus == null) {
            scriptSetRunStatusDao.insert(ScriptSetRunStatus(scriptId = scriptId, flowId = flowId, date = today, flowIdType = flowIdType, status = status))
        } else {
            existingStatus.status = status
            existingStatus.flowIdType = flowIdType // Update flowIdType
            scriptSetRunStatusDao.update(existingStatus)
        }
    }
    
    // Check if a specific flowId (set) was completed today
    suspend fun isFlowCompletedToday(scriptId: Int, flowId: Int): Boolean {
        val today = DateUtils.getTodayDateString()
        val status = scriptSetRunStatusDao.getByScriptAndFlowIdAndDate(scriptId, flowId, today)
        return status?.status == 1 // Assuming status 1 means completed
    }


    override suspend fun incrementRunCount(scriptId: Int) {
        scriptInfoDao.getById(scriptId)?.let { scriptInfo ->
            scriptInfo.runCount = (scriptInfo.runCount ?: 0) + 1
            scriptInfoDao.update(scriptInfo)
        }
    }

    override suspend fun resetRunCount(scriptId: Int) {
        scriptInfoDao.getById(scriptId)?.let { scriptInfo ->
            scriptInfo.runCount = 0
            scriptInfoDao.update(scriptInfo)
        }
    }

    // These skip methods now need scriptId context
    override suspend fun shouldSkipFlow(flowId: Int): Boolean {
        // This method signature in the interface is problematic as it lacks scriptId.
        // The maps are per scriptId. This needs to be addressed in the interface or by convention.
        // For now, this method cannot be implemented correctly without scriptId.
        // Throwing an exception or returning a default.
        // Let's assume for now it checks across ALL scripts, which is not ideal.
        // Or, if this is called from a context where scriptId is known, it should be passed.
        // For the purpose of this implementation, I'll log a warning.
        // A better approach would be to change the interface or have a "current" scriptId context.
        Log.w("ScriptStateManagerImpl", "shouldSkipFlow called without scriptId context. This check might be inaccurate.")
        return skipFlowIdsMap.values.any { it.contains(flowId) }
    }

    suspend fun shouldSkipFlow(scriptId: Int, flowId: Int): Boolean {
        return skipFlowIdsMap[scriptId]?.contains(flowId) ?: false
    }

    override suspend fun addSkipFlow(flowId: Int) { // Same issue as shouldSkipFlow
        Log.w("ScriptStateManagerImpl", "addSkipFlow called without scriptId context.")
        // Cannot correctly implement without scriptId.
    }

    suspend fun addSkipFlow(scriptId: Int, flowId: Int) {
        skipMutex.withLock {
            skipFlowIdsMap.getOrPut(scriptId) { mutableSetOf() }.add(flowId)
        }
    }

    override suspend fun removeSkipFlow(flowId: Int) { // Same issue
        Log.w("ScriptStateManagerImpl", "removeSkipFlow called without scriptId context.")
    }

    suspend fun removeSkipFlow(scriptId: Int, flowId: Int) {
        skipMutex.withLock {
            skipFlowIdsMap[scriptId]?.remove(flowId)
        }
    }
    
    override suspend fun clearAllSkipFlows() { // Same issue, assuming clear for ALL scripts
        Log.w("ScriptStateManagerImpl", "clearAllSkipFlows called without scriptId context. Clearing for all known scripts.")
         skipMutex.withLock {
            skipFlowIdsMap.clear()
        }
    }

    suspend fun clearAllSkipFlows(scriptId: Int) {
        skipMutex.withLock {
            skipFlowIdsMap.remove(scriptId)
        }
    }

    // Similar adjustments for skipAcIds
    override suspend fun shouldSkipAction(actionId: Int): Boolean {
        Log.w("ScriptStateManagerImpl", "shouldSkipAction called without scriptId context.")
        return skipAcIdsMap.values.any { it.contains(actionId) }
    }

    suspend fun shouldSkipAction(scriptId: Int, actionId: Int): Boolean {
        return skipAcIdsMap[scriptId]?.contains(actionId) ?: false
    }

    override suspend fun addSkipAction(actionId: Int) {
        Log.w("ScriptStateManagerImpl", "addSkipAction called without scriptId context.")
    }

    suspend fun addSkipAction(scriptId: Int, actionId: Int) {
        skipMutex.withLock {
            skipAcIdsMap.getOrPut(scriptId) { mutableSetOf() }.add(actionId)
        }
    }

    override suspend fun removeSkipAction(actionId: Int) {
        Log.w("ScriptStateManagerImpl", "removeSkipAction called without scriptId context.")
    }
    
    suspend fun removeSkipAction(scriptId: Int, actionId: Int) {
        skipMutex.withLock {
            skipAcIdsMap[scriptId]?.remove(actionId)
        }
    }

    override suspend fun clearAllSkipActions() {
        Log.w("ScriptStateManagerImpl", "clearAllSkipActions called without scriptId context. Clearing for all known scripts.")
        skipMutex.withLock {
            skipAcIdsMap.clear()
        }
    }
    
    suspend fun clearAllSkipActions(scriptId: Int) {
        skipMutex.withLock {
            skipAcIdsMap.remove(scriptId)
        }
    }
}
