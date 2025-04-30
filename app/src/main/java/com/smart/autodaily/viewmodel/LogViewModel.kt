package com.smart.autodaily.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.smart.autodaily.data.dataresource.LogDataSource
import com.smart.autodaily.handler.RunScript
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

class LogViewModel : ViewModel() {
    /*private val _logStateFlow = MutableStateFlow<List<String>>(emptyList())
    val logStateFlow: StateFlow<List<String>> = _logStateFlow*/
    private val _logStateFlow = MutableStateFlow<PagingData<String>>(PagingData.empty())
    val logStateFlow: StateFlow<PagingData<String>> = _logStateFlow

    private val _disableLog = MutableStateFlow(true)
    val disableLog: StateFlow<Boolean> = _disableLog

    fun loadLogs(){
        viewModelScope.launch {
            RunScript.initGlobalSet()
            val tempEnable = RunScript.globalSetMap.value[7]?.setValue?:"关闭"
            _disableLog.value = tempEnable == "关闭"
            if (_disableLog.value){
                return@launch
            }
            withContext(Dispatchers.IO){
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
