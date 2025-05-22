package com.smart.autodaily.feature.scripting.domain.interpreters

import com.smart.autodaily.command.Command
import com.smart.autodaily.data.entity.DetectResult
import com.smart.autodaily.data.entity.ScriptActionInfo
import com.smart.autodaily.feature.scripting.domain.command.CommandExecutor
import com.smart.autodaily.feature.scripting.domain.model.ScriptRunConfig

enum class ActionStatus {
    EXECUTED_CONTINUE,
    EXECUTED_FINISH_SET,
    NO_MATCH,
    CLICK_FAILED,
    CONDITION_NOT_MET
}

data class ActionResult(val status: ActionStatus, val nextDelay: Long = 0)

interface ActionInterpreter {
    suspend fun initializeAction(
        actionInfo: ScriptActionInfo,
        commands: List<Command>
    ): ScriptActionInfo

    suspend fun shouldExecute(
        actionInfo: ScriptActionInfo,
        detectedObjects: List<DetectResult>, // Non-text objects
        txtLabels: Array<Set<Short>>     // OCR results
    ): Boolean

    suspend fun executeActionCommands(
        actionInfo: ScriptActionInfo,
        detectedObjects: List<DetectResult>, // Non-text objects
        txtLabels: Array<Set<Short>>,    // OCR results
        config: ScriptRunConfig,
        commandExecutor: CommandExecutor
    ): ActionResult
}
