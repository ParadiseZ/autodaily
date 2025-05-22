package com.smart.autodaily.feature.scripting.domain.repository

import com.smart.autodaily.data.entity.ScriptActionInfo
import com.smart.autodaily.data.entity.ScriptSetInfo

interface ScriptConfigRepository {
    suspend fun getGlobalSettings(): Map<Int, ScriptSetInfo>
    suspend fun getScriptSettings(scriptId: Int): List<ScriptSetInfo>
    suspend fun getScriptActions(
        scriptId: Int,
        flowParentIdList: List<Int>,
        flowIdType: Int
    ): List<ScriptActionInfo>

    suspend fun getBackActions(scriptId: Int): List<ScriptActionInfo>
    suspend fun getAllActionsForScript(scriptId: Int): Map<Int, ScriptActionInfo>
}
