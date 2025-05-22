package com.smart.autodaily.feature.scripting.domain.command

import android.annotation.SuppressLint
import androidx.collection.IntSet
import com.smart.autodaily.data.entity.Point
import com.smart.autodaily.data.entity.Rect // Required for AdbSwipe
import com.smart.autodaily.data.entity.ScriptActionInfo
import com.smart.autodaily.handler.ActionHandler // Still used for randomXRange for now
import com.smart.autodaily.handler.INFO // For Lom logging
import com.smart.autodaily.handler.conf // For Sleep command
import com.smart.autodaily.handler.skipAcIds // Global state, to be refactored later
import com.smart.autodaily.handler.skipFlowIds // Global state, to be refactored later
import com.smart.autodaily.utils.Lom
// ShizukuUtil is removed as its usages should be replaced by CommandExecutor
// import com.smart.autodaily.utils.ShizukuUtil 
// partScope and launch removed as Reboot will use suspend fun
import kotlinx.coroutines.delay // For Sleep and potentially adbRebootApp

// Constants like TAP, START etc. are not directly used if CommandExecutor encapsulates these details.
// If CommandExecutor.executeShellCommand(rawCommand) is used, they might still be relevant.
// For now, assume CommandExecutor methods like executeAdbClick handle these.

// Helper functions are refactored to use CommandExecutor
// These might be better inside CommandExecutor or as private methods if only used by Reboot,
// but for now, they are top-level as in the original.

/**
 * Starts an application using the CommandExecutor.
 */
private suspend fun adbStartApp(packName: String, executor: CommandExecutor) {
    executor.executeAdbStartApp(packName)
}

/**
 * Reboots an application using the CommandExecutor.
 * This function assumes CommandExecutor.executeAdbRebootApp handles the sequence of
 * stopping the app, delaying, and then starting it.
 */
private suspend fun adbRebootApp(packName: String, executor: CommandExecutor) {
    // The original logic was:
    // ShizukuUtil.iUserService?.execVoidComand(STOP +packName.substring(0, packName.indexOf("/")))
    // delay(2000)
    // adbStartApp(packName)
    // This is now expected to be handled by executor.executeAdbRebootApp(packName)
    executor.executeAdbRebootApp(packName)
}


class Operation(val type: Int, val operation: Command) : Command {
    override suspend fun exec(sai: ScriptActionInfo, executor: CommandExecutor): Boolean {
        return operation.exec(sai, executor)
    }
}

class SkipFlowId(private val skipFlowId: Int) : Command {
    override suspend fun exec(sai: ScriptActionInfo, executor: CommandExecutor): Boolean {
        skipFlowIds.add(skipFlowId)
        Lom.d(INFO, "Command: Added skipFlowId $skipFlowId. Current: ${skipFlowIds.toArray().joinToString()}")
        return true
    }
}

class RmSkipFlowId(private val skipFlowId: Int) : Command {
    override suspend fun exec(sai: ScriptActionInfo, executor: CommandExecutor): Boolean {
        skipFlowIds.remove(skipFlowId)
        Lom.d(INFO, "Command: Removed skipFlowId $skipFlowId. Current: ${skipFlowIds.toArray().joinToString()}")
        return true
    }
}

class SkipAcId(private val skipAcId: Int) : Command {
    override suspend fun exec(sai: ScriptActionInfo, executor: CommandExecutor): Boolean {
        skipAcIds.add(skipAcId)
        Lom.d(INFO, "Command: Added skipAcId $skipAcId. Current: ${skipAcIds.toArray().joinToString()}")
        return true
    }
}

class RmSkipAcId(private val skipAcId: Int) : Command {
    override suspend fun exec(sai: ScriptActionInfo, executor: CommandExecutor): Boolean {
        skipAcIds.remove(skipAcId)
        Lom.d(INFO, "Command: Removed skipAcId $skipAcId. Current: ${skipAcIds.toArray().joinToString()}")
        return true
    }
}

class NotFlowId(val notFlowId: Int) : Command {
    override suspend fun exec(sai: ScriptActionInfo, executor: CommandExecutor): Boolean {
        // This command seems to be a placeholder or for conditional logic handled elsewhere.
        return true
    }
}

open class AdbClick(var point: Point? = null) : Command {
    override suspend fun exec(sai: ScriptActionInfo, executor: CommandExecutor): Boolean {
        var res = false
        val targetPoint = this.point ?: sai.point
        
        targetPoint?.let {
            // Apply randomness here if it's a policy for all AdbClick actions
            val clickPointWithRandomOffset = Point(
                it.x + ActionHandler.randomXRange,
                it.y + ActionHandler.randomYRange
            )
            Lom.d(INFO, "Command: AdbClick at $clickPointWithRandomOffset (original: $it)")
            executor.executeAdbClick(clickPointWithRandomOffset)
            if (this.point == null) { // only nullify if sai.point was used
                 sai.point = null
            }
            res = true
        } ?: Lom.w(INFO, "Command: AdbClick failed, no point specified.")
        return res
    }
}

class AdbPartClick(var type: String? = null, var part: Int = 0, var idx: Int = 0) : AdbClick() {
    // Inherits exec from AdbClick. If AdbPartClick has specific logic to determine the point,
    // it should override exec, calculate the point, and then call executor.executeAdbClick.
    // For now, assumes 'point' property is set before exec is called, or sai.point is used.
}

class AdbBack : Command {
    override suspend fun exec(sai: ScriptActionInfo, executor: CommandExecutor): Boolean {
        Lom.d(INFO, "Command: AdbBack")
        executor.executeAdbBack()
        return true
    }
}

// exeClick helper is removed as its logic is now inline in AdbClick or handled by CommandExecutor

class AdbSumClick(private val sumPointDelta: Point) : Command {
    override suspend fun exec(sai: ScriptActionInfo, executor: CommandExecutor): Boolean {
        var res = false
        sai.point?.let { currentPoint ->
            val newX = currentPoint.x + sumPointDelta.x
            val newY = currentPoint.y + sumPointDelta.y
            val targetPoint = Point(newX, newY)
            
            // Apply randomness, similar to AdbClick
            val clickPointWithRandomOffset = Point(
                targetPoint.x + ActionHandler.randomXRange,
                targetPoint.y + ActionHandler.randomYRange
            )
            Lom.d(INFO, "Command: AdbSumClick to $clickPointWithRandomOffset (original target: $targetPoint)")
            executor.executeAdbClick(clickPointWithRandomOffset)
            // Update sai.point to the new summed point *before* random offset
            sai.point = targetPoint 
            res = true
        } ?: {
            Lom.w(INFO, "Command: AdbSumClick failed, sai.point is null.")
            res = false
        }
        return res
    }
}

class AdbSwipe : Command {
    override suspend fun exec(sai: ScriptActionInfo, executor: CommandExecutor): Boolean {
        var res = true
        sai.swipePoint?.let {
            // Assuming swipePoint is a Rect: (x1, y1, x2, y2)
            // The original command was: "$SWIPE ${it.x} ${it.y} ${it.width} ${it.height} 1000"
            // This implies it.x, it.y are start points and it.width, it.height are end points.
            // This matches the CommandExecutor's executeAdbSwipe(rect: Rect?)
            Lom.d(INFO, "Command: AdbSwipe from (${it.x}, ${it.y}) to (${it.width}, ${it.height})")
            executor.executeAdbSwipe(it) // it should be a Rect(x1, y1, x2, y2)
        } ?: {
            Lom.w(INFO, "Command: AdbSwipe failed, sai.swipePoint is null.")
            res = false
        }
        return res
    }
}

class Skip : Command {
    override suspend fun exec(sai: ScriptActionInfo, executor: CommandExecutor): Boolean {
        Lom.d(INFO, "Command: Skip action ${sai.id} for flowId ${sai.flowId}, page ${sai.pageDesc}")
        sai.skipFlag = true // This state might be handled differently later
        return false // 'false' in original indicates to stop processing further actions in a set?
    }
}

class Sleep(private var time: Long = 1000L) : Command {
    @SuppressLint("DefaultLocale")
    override suspend fun exec(sai: ScriptActionInfo, executor: CommandExecutor): Boolean {
        Lom.d(
            INFO,
            "Command: Sleep for ${
                if (time > 60000) {
                    String.format("%.1f分钟", time.toDouble() / 60000.0)
                } else {
                    String.format("%.1f秒", time.toDouble() / 1000.0)
                }
            }"
        )
        // Original logic:
        // if (conf.capture?.isRecycled== false){
        //     conf.capture?.recycle()
        // }
        // This suggests a global bitmap `conf.capture` was being managed.
        // This specific bitmap management needs to be re-evaluated in the new architecture.
        // For now, the sleep itself is just a delay.
        // TODO: Address conf.capture recycling logic when screen capture is refactored.
        
        delay(time) // Use kotlinx.coroutines.delay for suspend function
        return true
    }
}

class Return(val type: String) : Command {
    override suspend fun exec(sai: ScriptActionInfo, executor: CommandExecutor): Boolean {
        Lom.d(INFO, "Command: Return, type '$type'")
        // This command's logic seems to be handled by the script execution engine based on its type.
        return true // Or false depending on how it affects execution flow
    }
}

class FinishFlowId(val flowId: Int = 0) : Command {
    override suspend fun exec(sai: ScriptActionInfo, executor: CommandExecutor): Boolean {
        Lom.d(INFO, "Command: FinishFlowId, flowId $flowId (sai.flowId: ${sai.flowId})")
        // This command likely signals the script execution engine.
        // Its effect (e.g., setting a flag like sai.finishFlag = true) will be determined by how
        // the ActionInterpreter uses this command's result or type.
        return true // Or specific value to indicate "finish this flow"
    }
}

/*
// Original Check command - commented out as it depends on appDb directly.
// This needs to be re-implemented via a repository or state manager if still needed.
class Check(private val setId : Int) : Command{
    override suspend fun exec(sai: ScriptActionInfo, executor: CommandExecutor): Boolean {
        // return appDb.scriptSetInfoDao.getResultFlag(setId) // Old direct DB access
        return true // Placeholder
    }
}
*/

class AddPosById(internal val saiId: Int) : Command {
    override suspend fun exec(sai: ScriptActionInfo, executor: CommandExecutor): Boolean {
        Lom.d(INFO, "Command: AddPosById, saiId $saiId. Current sai.point: ${sai.point}")
        // Original logic is missing, but name suggests it might modify sai.point
        // based on another ScriptActionInfo's point, fetched using saiId.
        // This would require access to other action data, possibly via ScriptConfigRepository.
        // TODO: Implement actual logic for AddPosById if it's used.
        return true
    }
}

class DropdownMenuNext(internal val setId: Int) : Command {
    override suspend fun exec(sai: ScriptActionInfo, executor: CommandExecutor): Boolean {
        Lom.d(INFO, "Command: DropdownMenuNext, setId $setId")
        // Original logic is missing. Name suggests interaction with a UI element.
        // This might involve complex UI state management or specific ADB commands.
        // TODO: Implement actual logic for DropdownMenuNext if it's used.
        return true
    }
}

class MinusPosById(internal val saiId: Int) : Command {
    override suspend fun exec(sai: ScriptActionInfo, executor: CommandExecutor): Boolean {
        Lom.d(INFO, "Command: MinusPosById, saiId $saiId. Current sai.point: ${sai.point}")
        // Similar to AddPosById, likely modifies sai.point.
        // TODO: Implement actual logic for MinusPosById if it's used.
        return true
    }
}

class Reboot(internal val pkgName: String) : Command {
    override suspend fun exec(sai: ScriptActionInfo, executor: CommandExecutor): Boolean {
        Lom.d(INFO, "Command: Reboot app $pkgName")
        adbRebootApp(pkgName, executor) // Call the refactored suspend function
        return true
    }
}

class RmSkipAcIdList(private val acids: IntSet) : Command {
    override suspend fun exec(sai: ScriptActionInfo, executor: CommandExecutor): Boolean {
        val beforeCount = skipAcIds.size()
        skipAcIds.removeAll(acids)
        Lom.d(INFO, "Command: RmSkipAcIdList. Removed ${beforeCount - skipAcIds.size()} IDs. Current: ${skipAcIds.toArray().joinToString()}")
        return true
    }
}
