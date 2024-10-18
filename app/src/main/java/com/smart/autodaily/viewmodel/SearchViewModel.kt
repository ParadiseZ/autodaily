package com.smart.autodaily.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.smart.autodaily.api.RemoteApi
import com.smart.autodaily.base.BaseViewModel
import com.smart.autodaily.data.dataresource.ScriptNetDataSource
import com.smart.autodaily.data.entity.ScriptInfo
import com.smart.autodaily.utils.PageUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchViewModel(application: Application)   : BaseViewModel(application = application)  {
    private val refreshRequests = Channel<Unit>(1)
    //远程数据
    private val _remoteScriptList = MutableStateFlow<PagingData<ScriptInfo>>(PagingData.empty())
    val remoteScriptList: StateFlow<PagingData<ScriptInfo>> = _remoteScriptList
    //搜索文本
    private val _searchText = MutableStateFlow("")
    //加载远程数据标志

    /*
    *处理搜索文本
    * */
    fun changeSearchText(key : String){
        this._searchText.value = key
    }
    /*
    * 搜索脚本信息，并和数据库比对，更新flow流以更新is_downloaded标志
    * */
    fun getRemoteScriptList(localList : List<ScriptInfo>) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    searchScriptByPage(localList).collectLatest {
                        _remoteScriptList.value = it
                    }
                }catch (e : Exception){
                    println(e.message)
                }
            }
        }
    }
    private fun searchScriptByPage(localList : List<ScriptInfo>): Flow<PagingData<ScriptInfo>> {
        val localIds = localList.map { it.scriptId }
        val netSearchResult = Pager(PagingConfig(pageSize = PageUtil.PAGE_SIZE, initialLoadSize =PageUtil.INITIALOAD_SIZE, prefetchDistance = PageUtil.PREFETCH_DISTANCE)) {
            ScriptNetDataSource(
                RemoteApi.searchDownRetrofit,
                _searchText.value
            )
        }
        val updatedNetSearchResult : Flow<PagingData<ScriptInfo>> = netSearchResult.flow.map { pagingData ->
            pagingData.map{ scriptInfo ->
                if (scriptInfo.scriptId in localIds){
                    scriptInfo.isDownloaded = 1
                    scriptInfo.downState = mutableIntStateOf(1)
                }else{
                    scriptInfo.downState = mutableIntStateOf(0)
                }
                scriptInfo.process = mutableIntStateOf(0)
                scriptInfo
            }
        }
        return updatedNetSearchResult.cachedIn(viewModelScope)
    }

    fun refresh() {
        refreshRequests.trySend(Unit)
    }
}