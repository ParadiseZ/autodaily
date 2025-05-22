package com.smart.autodaily.feature.scripting.domain.service

import android.content.Context
import android.graphics.Bitmap
import android.os.PowerManager
import android.util.Log
import com.smart.autodaily.BuildConfig
import com.smart.autodaily.data.entity.DetectResult
import com.smart.autodaily.data.entity.Point
import com.smart.autodaily.data.entity.Rect
import com.smart.autodaily.data.entity.ScriptActionInfo
import com.smart.autodaily.data.entity.ScriptInfo
import com.smart.autodaily.data.entity.ScriptSetInfo
import com.smart.autodaily.feature.scripting.domain.command.CommandExecutor
import com.smart.autodaily.feature.scripting.domain.interpreters.ActionInterpreter
import com.smart.autodaily.feature.scripting.domain.interpreters.ActionResult
import com.smart.autodaily.feature.scripting.domain.interpreters.ActionStatus
import com.smart.autodaily.feature.scripting.domain.model.ScriptRunConfig
import com.smart.autodaily.feature.scripting.domain.repository.ScriptConfigRepository
import com.smart.autodaily.feature.scripting.domain.state.ScriptExecutionState
import com.smart.autodaily.feature.scripting.domain.state.ScriptStateManager
import com.smart.autodaily.feature.scripting.domain.state.ScriptStateManagerImpl // For per-script skip methods
import com.smart.autodaily.handler.ActionString
import com.smart.autodaily.handler.CONF_REBOOT_DELAY
import com.smart.autodaily.handler.CONF_RETRY_BACK_ACTION
import com.smart.autodaily.handler.CONF_SCORE
import com.smart.autodaily.handler.CONF_TIME_OUT
import com.smart.autodaily.handler.INFO
// Lom was used but not imported, assuming it's meant to be com.smart.autodaily.utils.Lom
import com.smart.autodaily.utils.Lom // Added Lom import
import com.smart.autodaily.utils.ShizukuUtil // Placeholder for checking Shizuku availability
import com.smart.autodaily.core.capture.ScreenCaptureProvider
import kotlinx.coroutines.*
// android.util.Log was used but not imported for ShizukuCall debug logs
import android.util.Log // Added Log import for ShizukuCall debug logs
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.ConcurrentHashMap

class ScriptExecutorServiceImpl(
    private val context: Context,
    private val actionInterpreter: ActionInterpreter,
    private val modelDetectionService: ModelDetectionService,
    private val scriptConfigRepository: ScriptConfigRepository,
    private val scriptStateManager: ScriptStateManager, // The interface
    private val screenCaptureProvider: ScreenCaptureProvider,
    private val applicationScope: CoroutineScope // For launching long-running scripts
) : ScriptExecutorService, CommandExecutor {

    private val TAG = "ScriptExecutorService"
    private val activeScriptJobs = ConcurrentHashMap<Int, Job>()
    private val wakeLock: PowerManager.WakeLock by lazy {
        (context.getSystemService(Context.POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "${TAG}::ExecutorWakelock")
        }
    }

    // Assuming scriptStateManager is an instance of ScriptStateManagerImpl to access per-script skip methods.
    // This is a common pattern if the interface doesn't support all needed methods for an implementation.
    // A cleaner way would be to ensure the interface is complete or use a different internal mechanism.
    private val stateManagerImpl = scriptStateManager as? ScriptStateManagerImpl


    override suspend fun startExecution(script: ScriptInfo, initialConfig: ScriptRunConfig) {
        if (activeScriptJobs.containsKey(script.scriptId)) {
            Lom.w(TAG, "Script ${script.scriptId} is already running.")
            // Potentially update state to an error or warning that it's already running
            (scriptStateManager as? ScriptStateManagerImpl)?.updateScriptExecutionState(
                script.scriptId,
                ScriptExecutionState.Error("Already running")
            )
            return
        }

        val job = applicationScope.launch(Dispatchers.IO) { // Use Dispatchers.IO for blocking/long work
            try {
                // Acquire WakeLock
                if (!wakeLock.isHeld) {
                    wakeLock.acquire(3 * 60 * 60 * 1000L) // Timeout for 3 hours
                }
                Lom.i(TAG, "Starting execution for script: ${script.scriptName} (ID: ${script.scriptId})")
                updateState(script.scriptId, ScriptExecutionState.LoadingModel)

                // 1. Load Model
                // Assuming modelPath, paramPath, binPath are part of ScriptInfo or derived from it/global config
                // For now, using placeholders or values from initialConfig if they fit.
                // The original RunScript took these from global settings (conf object).
                // Let's assume these are now part of the ScriptRunConfig or ScriptInfo itself.
                val modelLoaded = modelDetectionService.loadModel(
                    modelPath = script.modelDir ?: "yolov8n", // Placeholder, needs to come from script properties
                    paramPath = script.modelParam ?: "model.param", // Placeholder
                    binPath = script.modelBin ?: "model.bin", // Placeholder
                    imgSize = initialConfig.imgSize,
                    useGpu = initialConfig.useGpu,
                    cpuThreadNum = initialConfig.cpuThreadNum,
                    cpuPower = initialConfig.cpuPower,
                    enableNHWC = initialConfig.enableNHWC,
                    enableDebug = initialConfig.enableDebug
                )

                if (!modelLoaded) {
                    Lom.e(TAG, "Failed to load model for script ${script.scriptId}.")
                    updateState(script.scriptId, ScriptExecutionState.Error("Model loading failed"))
                    return@launch
                }
                Lom.i(TAG, "Model loaded successfully for script ${script.scriptId}.")

                // 2. Fetch script configurations
                val scriptSets = scriptConfigRepository.getScriptSettings(script.scriptId)
                if (scriptSets.isEmpty()) {
                    Lom.w(TAG, "No script sets found for script ${script.scriptId}. Stopping.")
                    updateState(script.scriptId, ScriptExecutionState.Finished("No sets to run"))
                    return@launch
                }
                val backActionsList = scriptConfigRepository.getBackActions(script.scriptId)
                val allActionsMap = scriptConfigRepository.getAllActionsForScript(script.scriptId)


                // 3. Initialize ScriptRunConfig (merging global and script-specific)
                // The initialConfig is passed in, this might be the result of merging.
                // Or, we fetch global settings here and merge.
                // For now, assume initialConfig is the one to use.
                val config = initialConfig // Using the passed-in config for now.

                updateState(script.scriptId, ScriptExecutionState.Running("Starting main loop"))
                scriptStateManager.incrementRunCount(script.scriptId)

                var remRebootTime = config.rebootDelay * 60 * 1000L // Convert minutes to ms
                var noMatchCount = 0
                val maxNoMatchCount = 10 // From RunScript.kt (hardcoded)

                // Main loop through each ScriptSetInfo (Flow)
                for (currentSet in scriptSets) {
                    if (!isActive(script.scriptId)) break // Check if script execution was cancelled

                    Lom.i(TAG, "Processing set (flow): ${currentSet.setName} (ID: ${currentSet.setId}), Type: ${currentSet.flowIdType}")
                    updateState(script.scriptId, ScriptExecutionState.Running("Set: ${currentSet.setName}"))

                    // Check if flow should be skipped (via ScriptStateManager)
                    if (stateManagerImpl?.shouldSkipFlow(script.scriptId, currentSet.setId) == true) {
                        Lom.i(TAG, "Skipping flow ${currentSet.setId} as per skip list.")
                        scriptStateManager.updateScriptSetRunStatus(script.scriptId, currentSet.setId, currentSet.flowIdType, 2) // 2 for skip
                        continue
                    }
                    
                    // Check if flow was completed today (via ScriptStateManager, which uses DAO)
                    if (stateManagerImpl?.isFlowCompletedToday(script.scriptId, currentSet.setId) == true) {
                        Lom.i(TAG, "Skipping flow ${currentSet.setId} as it was completed today.")
                        continue // Already completed
                    }

                    // Fetch and initialize actions for the current set
                    // Original RunScript fetched actions here: appDb.scriptActionInfoDao.getCheckedBySetId(...)
                    // This seems to be covered by scriptConfigRepository.getScriptActions
                    val actionsForCurrentSetRaw = scriptConfigRepository.getScriptActions(
                        script.scriptId,
                        currentSet.flowParentIdList, // Assuming flowParentIdList is correctly populated in ScriptSetInfo
                        currentSet.flowIdType
                    )

                    if (actionsForCurrentSetRaw.isEmpty()){
                        Lom.w(TAG, "No actions found for set ${currentSet.setName}. Skipping set.")
                        continue
                    }
                    
                    val actionsForCurrentSet = actionsForCurrentSetRaw.mapNotNull { rawAction ->
                        allActionsMap[rawAction.id]?.let { fullActionDetails ->
                            actionInterpreter.initializeAction(fullActionDetails, emptyList()) // Commands will be parsed inside
                        }
                    }.sortedBy { it.sort }


                    var currentSetExecutionTime = 0L
                    val currentSetStartTime = System.currentTimeMillis()

                    // Inner loop for the current set (e.g., while current page/action not done)
                    while (isActive(script.scriptId)) {
                        val loopStartTime = System.currentTimeMillis()
                        if (currentSetExecutionTime > (currentSet.maxTime ?: 경쟁_SET_MAX_TIME_DEFAULT) * 1000L){ // 경쟁_SET_MAX_TIME_DEFAULT is placeholder
                             Lom.w(TAG, "Set ${currentSet.setName} exceeded maxTime. Moving to next set.")
                             break // Break from inner set loop
                        }

                        val screenBitmap = screenCaptureProvider.captureScreenBitmap()
                        if (screenBitmap == null) {
                            Lom.e(TAG, "Failed to capture screen. Retrying or stopping.")
                            updateState(script.scriptId, ScriptExecutionState.Error("Screen capture failed"))
                            delay(config.intervalTime) // Wait before retry or break
                            // TODO: Add retry limit for screen capture
                            continue // Or break, depending on retry strategy
                        }

                        val detectedObjects = modelDetectionService.detectObjects(screenBitmap, script.classesNum ?: 0)
                        
                        // Extract text labels from detectedObjects
                        // Based on: val txtRes = detectRes.filter { it.label == 0 }
                        // And then: txtRes.map { it.ocr!!.label = it.ocr.labelArr.toSet(); it.ocr.label }
                        val textRecognitionResults = detectedObjects.filter { it.label == 0 && it.ocr != null }
                        val txtLabels: Array<Set<Short>> = if (textRecognitionResults.isEmpty()) {
                            emptyArray()
                        } else {
                            textRecognitionResults.mapNotNull { detectResult ->
                                detectResult.ocr?.labelArr?.toSet() // labelArr is ShortArray, toSet() converts it
                            }.toTypedArray()
                        }
                        Lom.d(TAG, "Extracted ${txtLabels.size} text label sets from detected objects.")

                        var actionPerformedInLoop = false
                        for (actionInfo in actionsForCurrentSet) {
                            if (!isActive(script.scriptId)) break // Check before each action

                            if (stateManagerImpl?.shouldSkipAction(script.scriptId, actionInfo.id) == true) {
                                Lom.d(TAG, "Skipping action ${actionInfo.id} as per skip list.")
                                continue
                            }
                            
                            updateState(script.scriptId, ScriptExecutionState.Running("Action: ${actionInfo.pageDesc ?: actionInfo.id}"))

                            // Pass the extracted txtLabels to shouldExecute
                            if (actionInterpreter.shouldExecute(actionInfo, detectedObjects, txtLabels)) {
                                val actionResult = actionInterpreter.executeActionCommands(
                                    actionInfo,
                                    detectedObjects,
                                    txtLabels, // Pass txtLabels here as well
                                    config,
                                    this@ScriptExecutorServiceImpl // Pass self as CommandExecutor
                                )
                                actionPerformedInLoop = true
                                noMatchCount = 0 // Reset noMatchCount as an action was performed

                                if (actionResult.status == ActionStatus.EXECUTED_FINISH_SET) {
                                    Lom.i(TAG, "Set ${currentSet.setName} finished by command.")
                                    scriptStateManager.updateScriptSetRunStatus(script.scriptId, currentSet.setId, currentSet.flowIdType, 1) // 1 for completed
                                    // Break from action loop, then outer loop will break from set loop implicitly by this flag
                                    // This needs a flag to break outer loop or use labeled break.
                                    // For now, let's use a flag for the set.
                                    currentSet.isFlowSetFinish = true // Custom flag to break set loop
                                    break // Break from actions loop
                                }
                                if (actionResult.status == ActionStatus.CLICK_FAILED) {
                                     Lom.w(TAG, "Click failed for action ${actionInfo.id}. Trying back action or stopping set.")
                                     // TODO: Handle click failure (e.g. try back action)
                                     // For now, just break this action attempt.
                                     // If it was critical, the set might need to terminate.
                                }
                                
                                delay(actionResult.nextDelay) // Delay after action execution
                                break // Break from actions loop to re-evaluate screen (standard practice in such scripts)
                            }
                        } // End of actions loop for current set

                        if (currentSet.isFlowSetFinish == true) break // Break from inner set loop (while)

                        if (!actionPerformedInLoop) {
                            noMatchCount++
                            Lom.d(TAG, "No action matched in this iteration. NoMatchCount: $noMatchCount")
                            if (noMatchCount >= maxNoMatchCount) {
                                Lom.w(TAG, "Max no-match count reached. Trying back actions.")
                                if (config.tryBackAction) {
                                    // Simplified back action execution
                                    backActionsList.firstOrNull()?.let { backActionSai ->
                                        actionInterpreter.initializeAction(allActionsMap[backActionSai.id] ?: backActionSai, emptyList())
                                            .command.firstOrNull()?.exec(backActionSai, this@ScriptExecutorServiceImpl)
                                    }
                                }
                                noMatchCount = 0 // Reset after trying back action
                            }
                        }
                        
                        // Timeout and Reboot Logic (simplified)
                        val loopExecutionTime = System.currentTimeMillis() - loopStartTime
                        currentSetExecutionTime += loopExecutionTime
                        remRebootTime -= loopExecutionTime

                        if (remRebootTime <= 0) {
                            Lom.i(TAG, "Reboot timeout reached. Issuing reboot command.")
                            // CommandExecutor.executeAdbRebootApp(config.pkgName) // This is self call
                            this.executeAdbRebootApp(config.pkgName) // TODO: Ensure pkgName is in config
                            remRebootTime = config.rebootDelay * 60 * 1000L // Reset reboot timer
                        }
                        
                        delay(config.intervalTime) // Default interval between loops if no action taken or after action delay
                    } // End of inner set loop (while)
                    if (currentSet.isFlowSetFinish == true) {
                         Lom.i(TAG, "Set ${currentSet.setName} marked as finished. Updating status.")
                         scriptStateManager.updateScriptSetRunStatus(script.scriptId, currentSet.setId, currentSet.flowIdType, 1) // 1 for completed
                    }

                } // End of main loop (for each scriptSet)

                updateState(script.scriptId, ScriptExecutionState.Finished("All sets processed"))
                scriptStateManager.updateScriptRunStatus(script.scriptId, 0, 1) // 0 for flowIdType (overall), 1 for completed

            } catch (e: CancellationException) {
                Lom.i(TAG, "Script ${script.scriptId} execution was cancelled.")
                updateState(script.scriptId, ScriptExecutionState.Finished("Cancelled by user"))
            } catch (e: Exception) {
                Lom.e(TAG, "Error during script ${script.scriptId} execution: ${e.message}", e)
                updateState(script.scriptId, ScriptExecutionState.Error("Runtime error: ${e.message}"))
            } finally {
                Lom.i(TAG, "Execution ended for script: ${script.scriptName} (ID: ${script.scriptId})")
                activeScriptJobs.remove(script.scriptId)
                if (wakeLock.isHeld) {
                    wakeLock.release()
                }
                // Ensure overall state is recalculated if this was the last active script
                (scriptStateManager as? ScriptStateManagerImpl)?.updateScriptExecutionState(script.scriptId, scriptStateManager.getScriptExecutionState(script.scriptId).value)

            }
        }
        activeScriptJobs[script.scriptId] = job
    }

    private suspend fun updateState(scriptId: Int, state: ScriptExecutionState) {
        // Cast to Impl to use the more specific update method, or ensure interface has it.
        (scriptStateManager as? ScriptStateManagerImpl)?.updateScriptExecutionState(scriptId, state)
            ?: Lom.e(TAG, "Failed to cast ScriptStateManager to Impl for detailed state update.")
    }

    private fun isActive(scriptId: Int): Boolean {
        val job = activeScriptJobs[scriptId]
        return job?.isActive == true && currentCoroutineContext().isActive
    }
    
    // Placeholder for original RunScript.kt's 경쟁_SET_MAX_TIME_DEFAULT
    private val 경쟁_SET_MAX_TIME_DEFAULT = 300 // seconds, example value


    override suspend fun stopExecution(scriptId: Int) {
        activeScriptJobs[scriptId]?.let { job ->
            Lom.i(TAG, "Stopping script $scriptId")
            job.cancelAndJoin() // Cancel the job and wait for completion
            Lom.i(TAG, "Script $scriptId stopped.")
        } ?: Lom.w(TAG, "Script $scriptId not found or already stopped.")
        // State should be updated by the finally block in the job or explicitly if needed
        if (scriptStateManager.getScriptExecutionState(scriptId).value !is ScriptExecutionState.Finished &&
            scriptStateManager.getScriptExecutionState(scriptId).value !is ScriptExecutionState.Error) {
             updateState(scriptId, ScriptExecutionState.Finished("Stopped by user"))
        }
    }

    override fun getExecutionStateFlow(scriptId: Int): StateFlow<ScriptExecutionState> {
        return scriptStateManager.getScriptExecutionState(scriptId)
    }

    // --- CommandExecutor Implementation ---
    // Updated to use actual Shizuku calls and return Boolean for success/failure.

    private suspend fun executeShizukuCommand(command: String): Boolean {
        if (!ShizukuUtil.isGrant()) {
            Lom.e(TAG, "[CommandExecutor] Shizuku permission not granted.")
            return false
        }
        return try {
            Lom.i(TAG, "[CommandExecutor] Executing Shizuku command: $command")
            // Assuming ShizukuUtil.execute(command) is the method.
            // The exact return type or success indication of ShizukuUtil.execute is crucial.
            // For this example, we'll assume it's a method that doesn't return a value but throws on error.
            // Or, if it returns a result object, that would be checked here.
            // Example: val result = ShizukuUtil.execute(command); result.isSuccess
            ShizukuUtil.execute(command) // If this is void and throws on error.
            true // Assumed success if no exception.
        } catch (e: Exception) {
            Lom.e(TAG, "[CommandExecutor] Error executing command '$command': ${e.message}", e)
            false
        }
    }

    override suspend fun executeAdbClick(point: Point?): Boolean {
        if (point == null) {
            Lom.w(TAG, "[CommandExecutor] AdbClick failed: point is null.")
            return false
        }
        val command = "input tap ${point.x} ${point.y}"
        return executeShizukuCommand(command)
    }

    override suspend fun executeAdbSwipe(rect: Rect?): Boolean {
        if (rect == null) {
            Lom.w(TAG, "[CommandExecutor] AdbSwipe failed: rect is null.")
            return false
        }
        // Assuming rect.x, rect.y are start (x1,y1) and rect.width, rect.height are end (x2,y2) for swipe
        // The CommandExecutor interface defines Rect with x,y,width,height.
        // For swipe, this typically means (x1, y1, x2, y2) rather than (x,y,w,h from top-left).
        // Let's assume rect.width is x2 and rect.height is y2.
        val x1 = rect.x
        val y1 = rect.y
        val x2 = rect.width 
        val y2 = rect.height
        val durationMs = 200 // Default duration
        val command = "input swipe $x1 $y1 $x2 $y2 $durationMs"
        return executeShizukuCommand(command)
    }

    override suspend fun executeAdbBack(): Boolean {
        val command = "input keyevent BACK"
        return executeShizukuCommand(command)
    }

    override suspend fun executeAdbRebootApp(packageName: String): Boolean {
        val shortPkgName = if (packageName.contains("/")) packageName.substring(0, packageName.indexOf("/")) else packageName
        
        // Command to force stop the app
        val stopCommand = "am force-stop $shortPkgName"
        Lom.d(TAG, "[CommandExecutor] Attempting to stop app: $stopCommand")
        if (!executeShizukuCommand(stopCommand)) {
            Lom.w(TAG, "[CommandExecutor] Failed to stop app $shortPkgName. Continuing with start attempt.")
            // Depending on desired behavior, might return false here.
            // For now, we'll try to start it anyway.
        }
        
        delay(1000) // Delay after force-stop before starting

        // Command to start the main activity of the app
        // Using monkey command is a common way if the main activity name is unknown.
        val startCommand = "monkey -p $shortPkgName -c android.intent.category.LAUNCHER 1"
        Lom.d(TAG, "[CommandExecutor] Attempting to start app: $startCommand")
        return executeShizukuCommand(startCommand)
    }

    override suspend fun executeAdbStartApp(packageName: String): Boolean {
        // Using monkey command to launch the main activity.
        // If packageName here can be a component (e.g. com.example.app/.MainActivity),
        // then "am start -n componentName" would be more direct.
        // Assuming packageName is just the package string for monkey.
        val shortPkgName = if (packageName.contains("/")) packageName.substring(0, packageName.indexOf("/")) else packageName
        val command = "monkey -p $shortPkgName -c android.intent.category.LAUNCHER 1"
        return executeShizukuCommand(command)
    }
}

// Extension property to mark set finish, not ideal but helps break loop from inner logic
private var ScriptSetInfo.isFlowSetFinish: Boolean by java.util.WeakHashMap()
private const val 경쟁_SET_MAX_TIME_DEFAULT = 300 // Default max time for a set in seconds
```
