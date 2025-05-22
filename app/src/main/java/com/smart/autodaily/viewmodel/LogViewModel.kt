package com.smart.autodaily.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.smart.autodaily.data.dataresource.LogDataSource
import com.smart.autodaily.feature.scripting.domain.repository.ScriptConfigRepository
import com.smart.autodaily.feature.scripting.domain.usecase.InitializeGlobalScriptConfigUseCase
// import com.smart.autodaily.handler.RunScript // Replaced by UseCases/Repository
import com.smart.autodaily.utils.PageUtil.LOCAL_PAGE_SIZE
import com.smart.autodaily.utils.PageUtil.LOCAL_PRE_DISTANCE
import com.smart.autodaily.utils.deleteFile
import com.smart.autodaily.utils.logFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Assuming Hilt or a ViewModel Factory for injections
class LogViewModel(
    private val initializeGlobalScriptConfigUseCase: InitializeGlobalScriptConfigUseCase,
    private val scriptConfigRepository: ScriptConfigRepository
) : ViewModel() {
    private val _logStateFlow = MutableStateFlow<PagingData<String>>(PagingData.empty())
    val logStateFlow: StateFlow<PagingData<String>> = _logStateFlow

    private val _disableLog = MutableStateFlow(true)
    val disableLog: StateFlow<Boolean> = _disableLog

    // Define the key for the log enable/disable setting
    // This should ideally be a constant defined elsewhere (e.g., in a shared constants file)
    // For now, using the literal value 7 as seen in the original code.
    private val LOG_SETTING_ID = 7
    private val LOG_DISABLED_VALUE = "关闭" // Assuming "关闭" means disabled

    fun loadLogs() {
        viewModelScope.launch {
            // Ensure global settings are loaded
            initializeGlobalScriptConfigUseCase.execute()

            // Fetch the specific setting for logging
            val globalSettings = scriptConfigRepository.getGlobalSettings()
            val logSettingValue = globalSettings[LOG_SETTING_ID]?.setValue ?: LOG_DISABLED_VALUE
            _disableLog.value = (logSettingValue == LOG_DISABLED_VALUE)

            if (_disableLog.value) {
                // If logging is disabled, clear any existing paged data to prevent showing old logs
                _logStateFlow.value = PagingData.empty()
                return@launch
            }

            withContext(Dispatchers.IO) {
                Pager(
                    config = PagingConfig(
                        pageSize = LOCAL_PAGE_SIZE,
                        prefetchDistance = LOCAL_PRE_DISTANCE,
                        enablePlaceholders = false
                    ),
                    pagingSourceFactory = { LogDataSource(logFile.length()) }
                ).flow.cachedIn(viewModelScope).collectLatest {
                    _logStateFlow.value = it
                }
            }
        }
    }

    fun deleteLogs(){
        deleteFile(logFile)
    }
}
