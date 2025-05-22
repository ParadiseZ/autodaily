package com.smart.autodaily.feature.scripting.data.repository

import com.smart.autodaily.data.AppDb // Assuming AppDb is accessible like this
import com.smart.autodaily.data.dao.ScriptActionInfoDao
import com.smart.autodaily.data.dao.ScriptSetInfoDao
import com.smart.autodaily.data.entity.ScriptActionInfo
import com.smart.autodaily.data.entity.ScriptSetInfo
import com.smart.autodaily.feature.scripting.domain.repository.ScriptConfigRepository
import com.smart.autodaily.utils.DateUtils // Assuming isBetweenHour is in DateUtils
import java.util.Calendar

class ScriptConfigRepositoryImpl(
    private val scriptSetInfoDao: ScriptSetInfoDao = AppDb.INSTANCE.scriptSetInfoDao(),
    private val scriptActionInfoDao: ScriptActionInfoDao = AppDb.INSTANCE.scriptActionInfoDao()
) : ScriptConfigRepository {

    override suspend fun getGlobalSettings(): Map<Int, ScriptSetInfo> {
        return scriptSetInfoDao.getGlobalSet().associateBy { it.setId }
    }

    override suspend fun getScriptSettings(scriptId: Int): List<ScriptSetInfo> {
        val curFlowId = 0 // This was in the original RunScript.kt, seems to be a fixed value for this logic.
        
        // Fetch initial "max" sets (flowIdType = 1, typically "AND" conditions)
        val maxSet = scriptSetInfoDao.getScriptSetByScriptId(scriptId, curFlowId, 1).toMutableList()

        // Fetch "or" sets (flowIdType = 2)
        val orSet = scriptSetInfoDao.getScriptSetByScriptId(scriptId, curFlowId, 2)

        orSet.forEach {
            // In RunScript, it was: appDb.scriptSetInfoDao.getChildCheckedCount(it.scriptId, curFlowId, it.flowParentId!!) == 0
            // This check seems to imply that if an "OR" set has no checked children under a specific parent, it should be added.
            // It's a bit counter-intuitive as "OR" usually means any child.
            // Replicating the existing logic for now.
            if (scriptSetInfoDao.getChildCheckedCount(it.scriptId, curFlowId, it.flowParentId!!) == 0) {
                maxSet.add(it)
            }
        }
        
        // Filter sets based on parent flow ID checks and time-based conditions
        return maxSet.filter { setInfo ->
            // Parse flowParentIdList, default to empty list if flowParentId is null
            setInfo.flowParentIdList = setInfo.flowParentId?.split(",")?.mapNotNull { it.toIntOrNull() } ?: emptyList()
            
            // Condition from RunScript: flowParentIdList.size == appDb.scriptSetInfoDao.countCheckedNumByParentFlowId(it.scriptId, it.flowParentIdList)
            // This ensures all parent conditions specified in flowParentIdList are met.
            val allParentsChecked = if (setInfo.flowParentIdList.isEmpty()) {
                true // If no parents specified, condition is met
            } else {
                setInfo.flowParentIdList.size == scriptSetInfoDao.countCheckedNumByParentFlowId(setInfo.scriptId, setInfo.flowParentIdList)
            }
            
            allParentsChecked
        }.filter { item ->
            // Time-based filtering logic from RunScript
            // flowIdType 1: Morning (6-12)
            // flowIdType 2: Afternoon (12-18)
            // flowIdType 3: Other times
            // flowIdType 4: Always active (no time restriction)
            when {
                item.flowIdType == 4 -> true // Always active
                DateUtils.isBetweenHour(6, 12) -> item.flowIdType == 1
                DateUtils.isBetweenHour(12, 18) -> item.flowIdType == 2
                else -> item.flowIdType == 3 // Covers night and early morning before 6
            }
        }.sortedBy {
            it.sort // Sort by the 'sort' field
        }
    }

    override suspend fun getScriptActions(scriptId: Int, flowParentIdList: List<Int>, flowIdType: Int): List<ScriptActionInfo> {
        return scriptActionInfoDao.getCheckedBySetId(scriptId, flowParentIdList, flowIdType)
    }

    override suspend fun getBackActions(scriptId: Int): List<ScriptActionInfo> {
        return scriptActionInfoDao.getBackActions(scriptId)
    }

    override suspend fun getAllActionsForScript(scriptId: Int): Map<Int, ScriptActionInfo> {
        // Assuming scriptActionInfoDao.getAllActionsByScriptId(scriptId) exists or will be created.
        // This method should fetch all actions for a given scriptId.
        // If it doesn't exist, it would typically be:
        // @Query("SELECT * FROM script_action_info WHERE script_id = :scriptId")
        // fun getAllActionsByScriptId(scriptId: Int): List<ScriptActionInfo>
        return scriptActionInfoDao.getAllActionsByScriptId(scriptId).associateBy { it.id }
    }
}
