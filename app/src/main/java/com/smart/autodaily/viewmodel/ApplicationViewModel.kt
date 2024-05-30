package com.smart.autodaily.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smart.autodaily.data.appDb
import com.smart.autodaily.data.entity.ScriptInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ApplicationViewModel (application: Application) : AndroidViewModel(application) {
    /*private val _localScriptList = MutableStateFlow<PagingData<ScriptInfo>>(PagingData.empty())
    val localScriptList: StateFlow<PagingData<ScriptInfo>> = _localScriptList*/

    private val _localScriptAll = MutableStateFlow<List<ScriptInfo>>(emptyList())
    val localScriptListAll: StateFlow<List<ScriptInfo>> = _localScriptAll
    //加载本地数据的标志
    private val _loadDataFlagFlow = MutableStateFlow(false)
    val loadDataFlagFlow: StateFlow<Boolean> get() = _loadDataFlagFlow


    //加载本地所有数据
    fun loadScriptAll(){
        viewModelScope.launch {
            appDb!!.scriptInfoDao.getLocalScriptAll().collectLatest {
                _localScriptAll.value = it
            }
        }
    }
    /*//加载本地数据
    fun loadScripts(){
        viewModelScope.launch {
            getLocalScriptList().collectLatest {
                _localScriptList.value = it
            }
        }
    }
    private fun getLocalScriptList() : Flow<PagingData<ScriptInfo>> {
        return  Pager(
            PagingConfig(
            pageSize = PageUtil.PAGE_SIZE,
            initialLoadSize = PageUtil.INITIALOAD_SIZE,
            enablePlaceholders = true,
            prefetchDistance = PageUtil.PREFETCH_DISTANCE
        )
        ) {
            ScriptLocalDataSource()
        }.flow.cachedIn(viewModelScope)
    }*/
}