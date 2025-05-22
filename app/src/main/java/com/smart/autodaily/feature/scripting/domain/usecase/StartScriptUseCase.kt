package com.smart.autodaily.feature.scripting.domain.usecase

import com.smart.autodaily.data.entity.ScriptInfo
import com.smart.autodaily.feature.scripting.domain.model.ScriptRunConfig
import com.smart.autodaily.feature.scripting.domain.repository.ScriptConfigRepository
import com.smart.autodaily.feature.scripting.domain.service.ScriptExecutorService
import com.smart.autodaily.handler.CONF_REBOOT_DELAY
import com.smart.autodaily.handler.CONF_RETRY_BACK_ACTION
import com.smart.autodaily.handler.CONF_SCORE
import com.smart.autodaily.handler.CONF_TIME_OUT

class StartScriptUseCase(
    private val scriptExecutorService: ScriptExecutorService,
    private val scriptConfigRepository: ScriptConfigRepository
    // Consider injecting a global app config provider if specific default values are needed
) {
    suspend fun execute(script: ScriptInfo) {
        // Fetch global settings to build parts of ScriptRunConfig
        // The original RunScript.conf used globalSetMap for these values.
        val globalSettings = scriptConfigRepository.getGlobalSettings()

        // Create ScriptRunConfig by merging script-specific details and global/default values.
        // This is an example; the actual structure of ScriptInfo and how it maps to
        // ScriptRunConfig needs to be based on the available fields.
        // Default values are taken from original RunScript.conf or common practice.
        
        val intervalTime = globalSettings[CONF_TIME_OUT]?.setValue?.toLongOrNull() ?: 2000L // Default 2s
        val similarScore = globalSettings[CONF_SCORE]?.setValue?.toFloatOrNull() ?: 0.85f // Default 0.85f
        // pkgName might come from ScriptInfo or a more specific part of the script's configuration
        val pkgName = script.pkgName ?: globalSettings[ActionString.PKG_NAME]?.setValue ?: "com.example.default" // Placeholder
        val rebootDelay = globalSettings[CONF_REBOOT_DELAY]?.setValue?.toLongOrNull() ?: 120L // Default 120 minutes
        val tryBackAction = globalSettings[CONF_RETRY_BACK_ACTION]?.setValue?.toBoolean() ?: true // Default true
        
        // These might be more specific to the model or script settings in ScriptInfo
        val useGpu = script.useGpu ?: true // Default to true, or from global settings if applicable
        val imgSize = script.imgSize ?: 640 // Default, or from global settings

        // Default values for model parameters (from ScriptRunConfig definition)
        val cpuThreadNum = 2
        val cpuPower = 0
        val enableNHWC = true
        val enableDebug = false


        val config = ScriptRunConfig(
            intervalTime = intervalTime,
            similarScore = similarScore,
            pkgName = pkgName,
            rebootDelay = rebootDelay,
            tryBackAction = tryBackAction,
            useGpu = useGpu,
            imgSize = imgSize,
            cpuThreadNum = cpuThreadNum,
            cpuPower = cpuPower,
            enableNHWC = enableNHWC,
            enableDebug = enableDebug
            // Other parameters like model paths, specific script arguments might be part of ScriptInfo
            // and used directly by ScriptExecutorService when loading model or actions.
        )

        scriptExecutorService.startExecution(script, config)
    }
}
