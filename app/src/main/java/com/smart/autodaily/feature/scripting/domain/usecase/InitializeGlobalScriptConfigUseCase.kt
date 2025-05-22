package com.smart.autodaily.feature.scripting.domain.usecase

import com.smart.autodaily.feature.scripting.domain.repository.ScriptConfigRepository
import com.smart.autodaily.utils.Lom

class InitializeGlobalScriptConfigUseCase(
    private val scriptConfigRepository: ScriptConfigRepository
) {
    suspend fun execute() {
        try {
            // Fetching global settings. The repository might cache them or always fetch.
            // The purpose here is to ensure they are loaded/initialized if needed.
            // If the global settings are used by other parts of the app immediately after this,
            // this call ensures they are populated in the repository's cache if it has one.
            scriptConfigRepository.getGlobalSettings()
            Lom.i("InitializeGlobalScriptConfigUseCase", "Global script configurations initialized/fetched.")
        } catch (e: Exception) {
            Lom.e("InitializeGlobalScriptConfigUseCase", "Error initializing global script configurations: ${e.message}", e)
            // Depending on the app's requirements, this might throw the exception
            // or handle it (e.g., by ensuring some default config is available).
        }
    }
}
