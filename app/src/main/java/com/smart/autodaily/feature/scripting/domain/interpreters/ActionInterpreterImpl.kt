package com.smart.autodaily.feature.scripting.domain.interpreters

import android.graphics.Bitmap
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.smart.autodaily.data.entity.DetectResult
import com.smart.autodaily.data.entity.Point
import com.smart.autodaily.data.entity.Rect
import com.smart.autodaily.data.entity.ScriptActionInfo
import com.smart.autodaily.feature.scripting.domain.command.*
import com.smart.autodaily.feature.scripting.domain.command.CommandExecutor
import com.smart.autodaily.feature.scripting.domain.model.ScriptRunConfig
import com.smart.autodaily.handler.ActionString
import com.smart.autodaily.handler.INFO
import com.smart.autodaily.navpkg.AutoDaily // For hsvToColor, label processing
import com.smart.autodaily.utils.Lom
import com.smart.autodaily.utils.ScreenUtils // For getScreenHash
import com.smart.autodaily.utils.Utils // For isJson

// Placeholder for screen capture utility
interface ScreenCaptureProvider {
    suspend fun captureScreenBitmap(): Bitmap?
    // Placeholder for getting specific pixel colors.
    // A real implementation would need to efficiently get color data from the screen.
    // This might involve platform-specific code or interaction with a root service.
    suspend fun getPixelColor(x: Int, y: Int): Triple<Int, Int, Int>? // R, G, B
    suspend fun getRegionColors(centerX: Int, centerY: Int, regionWidth: Int, regionHeight: Int): List<Triple<Int, Int, Int>>
}


class ActionInterpreterImpl(
    private val model: AutoDaily, // Injected for hsvToColor and text processing
    private val screenCaptureProvider: ScreenCaptureProvider, // Injected for screen access
    private val gson: Gson = Gson() // For parsing JSON in actionString/setValue
) : ActionInterpreter {

    companion object {
        private const val TAG = "ActionInterpreterImpl"
        private const val MAX_CLICK_RETRY = 3
        private const val DEFAULT_COLOR_CHECK_REGION_SIZE = 10 // Default 10x10 region for color check
    }

    override suspend fun initializeAction(
        actionInfo: ScriptActionInfo,
        commandsFromParam: List<com.smart.autodaily.feature.scripting.domain.command.Command> // Renamed to avoid confusion
    ): ScriptActionInfo {
        Lom.d(TAG, "Initializing action ${actionInfo.id} (${actionInfo.pageDesc}): ${actionInfo.actionString}, setValue: ${actionInfo.setValue}")

        // Clear previous transient state
        actionInfo.command.clear()
        actionInfo.intLabelSet = setOf()
        actionInfo.intExcLabelSet = setOf()
        actionInfo.txtLabelSet = listOf()
        actionInfo.txtExcLabelSet = listOf()
        actionInfo.hsv = setOf()
        // actionInfo.hsvExc = setOf() // TODO: Add hsvExc processing if used

        var actionStr = actionInfo.actionString // Might be JSON or simple string
        var valueStr = actionInfo.setValue

        // Logic from RunScript.initActionFun to parse actionString and setValue
        if (Utils.isJson(actionStr)) {
            try {
                val json = gson.fromJson(actionStr, JsonObject::class.java)
                actionStr = json.get("curAction").asString // "curAction" field from original
                valueStr = json.get("value").toString() // "value" field as JSON string

                actionInfo.intLabel = json.get("intLabel")?.asString
                actionInfo.intExcLabel = json.get("intExcLabel")?.asString
                actionInfo.txtLabel = json.get("txtLabel")?.asString
                actionInfo.txtExcLabel = json.get("txtExcLabel")?.asString
                actionInfo.rgb = json.get("rgb")?.asString
                // actionInfo.rgbExc = json.get("rgbExc")?.asString // TODO
                actionInfo.labelPos = json.get("labelPos")?.asInt ?: 0
                actionInfo.operTxt = json.get("operTxt")?.asBoolean ?: false
                
                // Parse commands from JSON array in "command" field if curAction is "COMMAND_LIST" (example name)
                // The original initActionFun had complex logic for parsing commands from JSON.
                // This part needs to reflect how commands were defined in actionString's JSON.
                if (json.has("commands") && json.get("commands").isJsonArray) {
                     val cmdArray = json.getAsJsonArray("commands")
                     parseCommandsFromJsonArray(cmdArray, actionInfo)
                }

            } catch (e: Exception) {
                Lom.e(TAG, "Error parsing JSON actionString for action ${actionInfo.id}: ${e.message}")
            }
        }
        
        // If commands were not populated from JSON, try parsing from actionStr (simple type) or commandsFromParam
        if (actionInfo.command.isEmpty()) {
            if (commandsFromParam.isNotEmpty()) { // Prefer pre-parsed commands if available and no JSON commands found
                actionInfo.command.addAll(commandsFromParam)
                Lom.d(TAG, "Using ${commandsFromParam.size} pre-parsed commands for action ${actionInfo.id}.")
            } else {
                 // Fallback to simple parsing of actionStr (if not JSON or JSON parsing failed to add commands)
                 parseSimpleActionString(actionStr, valueStr, actionInfo)
            }
        }


        // Populate label sets (common logic from initActionFun)
        actionInfo.intLabel?.split(',')?.mapNotNull { it.trim().toIntOrNull() }?.let {
            if (it.isNotEmpty()) {
                actionInfo.intFirstLab = it[0] // Used for color check target
                actionInfo.intLabelSet = it.toSet()
            }
        }
        actionInfo.intExcLabel?.split(',')?.mapNotNull { it.trim().toIntOrNull() }?.let {
            actionInfo.intExcLabelSet = it.toSet()
        }
        
        // Populate text labels (example from initActionFun, using model.strToShortSet)
        actionInfo.txtLabel?.let {
            actionInfo.txtLabelSet = it.split("&&").map { part ->
                model.strToShortSet(part.trim()) // Uses AutoDaily model instance
            }
        }
        actionInfo.txtExcLabel?.let {
            actionInfo.txtExcLabelSet = it.split("&&").map { part ->
                model.strToShortSet(part.trim()) // Uses AutoDaily model instance
            }
        }
        // Initialize txtFirstLab if needed (e.g., actionInfo.txtFirstLab = actionInfo.txtLabelSet.firstOrNull() ?: emptySet())


        // Populate HSV (from original initActionFun, using model.hsvToColor)
        actionInfo.rgb?.let { rgbString ->
            // Assuming rgbString is "r1,g1,b1|r2,g2,b2" or just "r,g,b"
            val hsvSet = mutableSetOf<Int>()
            rgbString.split('|').forEach { colorStr ->
                val components = colorStr.split(',').mapNotNull { it.trim().toIntOrNull() }
                if (components.size == 3) {
                    hsvSet.add(model.hsvToColor(components[0], components[1], components[2]))
                }
            }
            actionInfo.hsv = hsvSet
        }
        // TODO: Process actionInfo.rgbExc for hsvExc similarly

        Lom.d(TAG, "Initialized action ${actionInfo.id}: ${actionInfo.command.size} commands, labels: ${actionInfo.intLabelSet}, textLabels: ${actionInfo.txtLabelSet.size}, hsv: ${actionInfo.hsv}")
        return actionInfo
    }

    // Helper to parse commands from actionString if it's not JSON / commandsFromParam is empty
    private fun parseSimpleActionString(actionType: String?, value: String?, actionInfo: ScriptActionInfo) {
        Lom.d(TAG, "Parsing simple actionString: $actionType, value: $value")
        when (actionType) {
            ActionString.CLICK -> {
                val point = if (Utils.isJson(value)) gson.fromJson(value, Point::class.java) else actionInfo.point
                actionInfo.command.add(AdbClick(point ?: actionInfo.point)) // Use point from SAI if not in value
            }
            ActionString.SWIPE -> {
                 val rect = if (Utils.isJson(value)) gson.fromJson(value, Rect::class.java) else actionInfo.swipePoint
                 actionInfo.swipePoint = rect // Ensure sai.swipePoint is set for AdbSwipe command
                 actionInfo.command.add(AdbSwipe())
            }
            ActionString.BACK -> actionInfo.command.add(AdbBack())
            ActionString.SLEEP -> {
                val time = value?.toLongOrNull() ?: actionInfo.setValue?.toLongOrNull() ?: 1000L
                actionInfo.command.add(Sleep(time))
            }
            ActionString.REBOOT -> {
                value?.let { actionInfo.command.add(Reboot(it)) } ?: Lom.w(TAG, "Reboot command missing package name in value")
            }
            ActionString.FINISH_SET -> actionInfo.command.add(FinishFlowId(actionInfo.flowId))
            ActionString.SKIP_CUR_ACTION -> actionInfo.command.add(Skip())
            // Add other simple commands from ActionString constants
            else -> {
                Lom.w(TAG, "Unknown or complex command type in simple actionString: $actionType. Requires JSON parsing or commandsFromParam.")
                // If actionType might be a list like "CLICK|SLEEP", split and parse recursively.
                // This was a simplified path in the previous turn, full parsing is more complex.
                // The original initActionFun handled many more cases.
            }
        }
    }
    
    // Helper to parse commands from a JSON array (called from initializeAction if actionString is JSON with "commands")
    private fun parseCommandsFromJsonArray(jsonArray: JsonArray, actionInfo: ScriptActionInfo) {
        // This needs to map JSON objects in the array to specific Command instances.
        // Example: { "type": "CLICK", "params": {"x":10, "y":20} } -> AdbClick(Point(10,20))
        // This is highly dependent on the exact JSON structure used in the original app.
        // This is a placeholder for that complex parsing logic.
        Lom.w(TAG, "parseCommandsFromJsonArray is a placeholder. Full JSON command parsing not yet implemented.")
        // Example structure:
        // for (element in jsonArray) {
        //     val jsonObj = element.asJsonObject
        //     val type = jsonObj.get("type")?.asString ?: continue
        //     val params = jsonObj.getAsJsonObject("params")
        //     when (type) {
        //         "CLICK" -> { /* parse Point from params */ actionInfo.command.add(AdbClick(...)) }
        //         "SLEEP" -> { /* parse time from params */ actionInfo.command.add(Sleep(...)) }
        //         // ... etc.
        //     }
        // }
    }


    override suspend fun shouldExecute(
        actionInfo: ScriptActionInfo,
        detectedObjects: List<DetectResult>,
        textLabels: Array<Set<Short>> // From TextRecognition, array of sets for different recognition types/results
    ): Boolean {
        var isMatch = true
        // Integer Label Matching (from RunScript.isMatch)
        if (actionInfo.intLabelSet.isNotEmpty()) {
            val detectedLabels = detectedObjects.map { it.label }.toSet()
            isMatch = if (actionInfo.labelPos == 0) { // AND logic: all labels must match
                detectedLabels.containsAll(actionInfo.intLabelSet)
            } else { // OR logic: any label must match
                actionInfo.intLabelSet.any { detectedLabels.contains(it) }
            }
            if (!isMatch) Lom.d(TAG, "shouldExecute: intLabelSet match failed for action ${actionInfo.id}")
        }

        // Integer Exclude Label Matching
        if (isMatch && actionInfo.intExcLabelSet.isNotEmpty()) {
            val detectedLabels = detectedObjects.map { it.label }.toSet()
            if (detectedLabels.none { actionInfo.intExcLabelSet.contains(it) }) {
                // isMatch remains true, no excluded labels found
            } else {
                isMatch = false // Found an excluded label
                Lom.d(TAG, "shouldExecute: intExcLabelSet match failed (excluded label found) for action ${actionInfo.id}")
            }
        }

        // Text Label Matching (from RunScript.isMatch, complex part)
        // actionInfo.txtLabelSet is List<Set<Short>> (parsed from "textA&&textB")
        // textLabels is Array<Set<Short>> (from screen, e.g. textLabels[0] is primary OCR result)
        if (isMatch && actionInfo.txtLabelSet.isNotEmpty()) {
            // Original logic compared actionInfo.txtLabelSet with each set in textLabels.
            // It was often textLabels[0] (primary OCR) vs actionInfo.txtLabelSet[0] (primary condition)
            // And actionInfo.labelPos determined AND/OR logic for multiple text conditions in txtLabelSet.
            // Assuming labelPos applies to text labels as well if multiple are defined in actionInfo.txtLabelSet.
            val primaryOcrResult = textLabels.getOrNull(0) ?: emptySet() // Safely get primary OCR result
            
            if (actionInfo.labelPos == 0) { // AND: All text conditions in txtLabelSet must be met by primaryOcrResult
                isMatch = actionInfo.txtLabelSet.all { conditionSet -> primaryOcrResult.containsAll(conditionSet) }
            } else { // OR: Any text condition in txtLabelSet must be met by primaryOcrResult
                isMatch = actionInfo.txtLabelSet.any { conditionSet -> primaryOcrResult.containsAll(conditionSet) }
            }
            if (!isMatch) Lom.d(TAG, "shouldExecute: txtLabelSet match failed for action ${actionInfo.id}")
        }
        
        // Text Exclude Label Matching
        if (isMatch && actionInfo.txtExcLabelSet.isNotEmpty()) {
            val primaryOcrResult = textLabels.getOrNull(0) ?: emptySet()
            // If any of the exclusion sets are found in OCR, then it's not a match.
            if (actionInfo.txtExcLabelSet.any { exclusionSet -> primaryOcrResult.containsAll(exclusionSet) }) {
                isMatch = false
                Lom.d(TAG, "shouldExecute: txtExcLabelSet match failed (excluded text found) for action ${actionInfo.id}")
            }
        }

        if (!isMatch) return false // Early exit if label/text matching fails

        // Color Check (from tryAction's condition, using helper)
        if (actionInfo.hsv.isNotEmpty()) { // Only check color if HSV conditions are set
            if (!checkColorInternal(actionInfo, detectedObjects)) {
                Lom.d(TAG, "shouldExecute: checkColorInternal failed for action ${actionInfo.id}")
                return false
            }
        }
        // TODO: Add hsvExc (excluded colors) check if necessary

        Lom.d(TAG, "shouldExecute: true for action ${actionInfo.id}")
        return true
    }

    private suspend fun checkColorInternal(
        sai: ScriptActionInfo,
        detectRes: List<DetectResult>
    ): Boolean {
        if (sai.hsv.isEmpty()) return true // No color condition, so it's a "match" in terms of color.

        // Determine target point for color check.
        // If intFirstLab is set and corresponds to a detected object, use its center.
        // Otherwise, if sai.point is set (e.g. for a fixed-point color check), use that.
        var checkPoint: Point? = null
        if (sai.intFirstLab != 0) { // Assuming 0 means no specific label, or intFirstLab is actual label ID
            val targetLabelResult = detectRes.firstOrNull { it.label == sai.intFirstLab }
            if (targetLabelResult != null) {
                checkPoint = Point(
                    targetLabelResult.rectF.centerX().toInt(),
                    targetLabelResult.rectF.centerY().toInt()
                )
            } else {
                // intFirstLab was specified, but no such object detected. Color check fails.
                Lom.d(TAG, "checkColorInternal: intFirstLab ${sai.intFirstLab} not found in detected objects.")
                return false
            }
        } else if (sai.point != null) { // Use action's own point if no label-based target
            checkPoint = sai.point
        }

        if (checkPoint == null) {
            Lom.w(TAG, "checkColorInternal: No valid point (from label or sai.point) to check color for action ${sai.id}.")
            return false // Cannot check color without a point
        }

        // Use ScreenCaptureProvider to get colors in a region around checkPoint
        // sai.colorRegion might define offset & size, or use defaults.
        // For now, using a small default region around the checkPoint.
        val regionWidth = DEFAULT_COLOR_CHECK_REGION_SIZE
        val regionHeight = DEFAULT_COLOR_CHECK_REGION_SIZE
        
        // getRegionColors should ideally be efficient. For this placeholder, it might be slow.
        val pixelRGBs = screenCaptureProvider.getRegionColors(checkPoint.x, checkPoint.y, regionWidth, regionHeight)
        
        if (pixelRGBs.isEmpty()) {
            Lom.w(TAG, "checkColorInternal: Could not get pixel colors from region for action ${sai.id}.")
            return false // Failed to get color data
        }

        return pixelRGBs.any { (r, g, b) ->
            val hsvPixel = model.hsvToColor(r, g, b) // Convert sampled R,G,B to model's HSV int
            sai.hsv.contains(hsvPixel) // Check if this HSV value is in the action's required set
        }
    }


    override suspend fun executeActionCommands(
        actionInfo: ScriptActionInfo,
        detectedObjects: List<DetectResult>, // Non-text objects
        txtLabels: Array<Set<Short>>,    // OCR results - new parameter
        config: ScriptRunConfig,
        commandExecutor: CommandExecutor
    ): ActionResult {
        Lom.d(TAG, "Executing commands for action ${actionInfo.id} (${actionInfo.pageDesc}), with ${txtLabels.size} text label sets.")
        var overallStatus = ActionStatus.EXECUTED_CONTINUE
        var cumulativeDelay = 0L 

        for (command in actionInfo.command) {
            Lom.d(TAG, "Executing command: ${command::class.java.simpleName}")
            var commandExecutedSuccessfully = true

            if (command is AdbClick || command is AdbSumClick) {
                var clickSuccess = false
                var tryCount = 0
                var originalScreenHash: String? = null

                if (config.tryBackAction) { // Assuming tryBackAction in config implies screen hash check enabled
                    val initialBitmap = screenCaptureProvider.captureScreenBitmap()
                    originalScreenHash = initialBitmap?.let { ScreenUtils.getScreenHash(it) } // Needs ScreenUtils
                    Lom.d(TAG, "Original screen hash for click: $originalScreenHash")
                }

                while (tryCount < MAX_CLICK_RETRY) {
                    tryCount++
                    Lom.d(TAG, "Attempting click (try #$tryCount): ${command::class.java.simpleName}")
                    command.exec(actionInfo, commandExecutor) 

                    if (config.tryBackAction && originalScreenHash != null) {
                        delay(500) // Wait for screen to potentially change
                        val newBitmap = screenCaptureProvider.captureScreenBitmap()
                        val newScreenHash = newBitmap?.let { ScreenUtils.getScreenHash(it) }
                        Lom.d(TAG, "New screen hash after click: $newScreenHash")
                        if (newScreenHash != null && newScreenHash != originalScreenHash) {
                            Lom.i(TAG, "Screen changed after click, click successful.")
                            clickSuccess = true
                            break
                        } else {
                            Lom.w(TAG, "Screen did not change after click (or hash check failed). Retrying if possible.")
                            if (tryCount < MAX_CLICK_RETRY) delay(config.intervalTime)
                        }
                    } else { // No screen hash check, assume success
                        clickSuccess = true
                        break 
                    }
                }
                if (!clickSuccess) {
                    Lom.w(TAG, "Click command failed after $MAX_CLICK_RETRY retries for action ${actionInfo.id}.")
                    overallStatus = ActionStatus.CLICK_FAILED
                    commandExecutedSuccessfully = false
                }
            } else {
                // For non-click commands or if screen hash check is off
                if (!command.exec(actionInfo, commandExecutor)) {
                    // Original Skip command returned false. This might indicate the action sequence should stop.
                    // If exec returns false, we might need to interpret it based on command type.
                    Lom.d(TAG, "Command ${command::class.java.simpleName} returned false.")
                    if (command is Skip) {
                         // If a Skip command returns false, it means "action processed as a skip".
                         // The overallStatus might still be EXECUTED_CONTINUE for the set.
                         // Or a specific "SKIPPED_ACTION" status if that's useful.
                    } else {
                        // For other commands, 'false' might mean a failure or a specific flow control.
                        // This part is ambiguous without knowing each command's 'false' semantics.
                        // For now, assume 'false' means the command itself failed if not 'Skip'.
                        commandExecutedSuccessfully = false 
                        // overallStatus = ActionStatus.CONDITION_NOT_MET or specific error?
                    }
                }
            }

            if (!commandExecutedSuccessfully && overallStatus != ActionStatus.CLICK_FAILED) {
                // If a non-click command failed, update status (unless already click_failed)
                // This is a generic failure. More specific statuses might be needed.
                overallStatus = ActionStatus.CONDITION_NOT_MET // Or a more generic "COMMAND_FAILED"
            }

            if (overallStatus == ActionStatus.CLICK_FAILED || (overallStatus == ActionStatus.CONDITION_NOT_MET && command !is Skip)) {
                 break // Stop processing further commands for this action if a critical failure occurred
            }
            
            // Handle commands that affect flow control or state explicitly
            if (command is FinishFlowId) {
                Lom.i(TAG, "FinishFlowId command encountered for flow ${actionInfo.flowId}.")
                overallStatus = ActionStatus.EXECUTED_FINISH_SET
                break 
            }
            if (command is Sleep) {
                // The sleep itself is handled by the command's exec method (using delay).
                // cumulativeDelay += command.time // No, Sleep command itself delays. This is post-action delay.
            }
        }
        
        val nextDelay = if (cumulativeDelay > 0) cumulativeDelay else config.intervalTime
        
        Lom.d(TAG, "Finished executing commands for action ${actionInfo.id}. Status: $overallStatus, Next Delay: $nextDelay")
        return ActionResult(overallStatus, nextDelay)
    }
}
