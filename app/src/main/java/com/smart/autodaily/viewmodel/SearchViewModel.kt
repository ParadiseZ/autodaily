package com.smart.autodaily.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.smart.autodaily.api.RemoteApi
import com.smart.autodaily.api.SearchDownloadApi
import com.smart.autodaily.data.appDb
import com.smart.autodaily.data.dataresource.ScriptLocalDataSource
import com.smart.autodaily.data.dataresource.ScriptNetDataSource
import com.smart.autodaily.data.entity.ScriptInfo
import com.smart.autodaily.data.entity.ScriptSetInfo
import com.smart.autodaily.retrofit2.RetrofitCreate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel()  {
    var refreshing  =  mutableStateOf(false)

    /*
    * 搜索脚本信息，并和数据库比对，更新flow流以更新is_downloaded标志
    * */
    fun searchScriptByPage(): Flow<PagingData<ScriptInfo>> {
        var updatedNetSearchResult : Flow<PagingData<ScriptInfo>>? = null
        val netSearchResult = Pager(PagingConfig(pageSize = 10, initialLoadSize =20,enablePlaceholders = true, prefetchDistance = 3)) {
            ScriptNetDataSource(
                RemoteApi.searchDownRetrofit
            )
        }
        val localSearchResult = appDb?.scriptInfoDao?.getScriptIdAll()
        localSearchResult?.let {
            updatedNetSearchResult =netSearchResult.flow.map { pagingData ->
                pagingData.map{ scriptInfo ->
                    if (scriptInfo.script_id in localSearchResult){
                        scriptInfo.is_downloaded = 1
                    }
                    scriptInfo
                }
            }
        }
        return if (updatedNetSearchResult == null){
            netSearchResult.flow.cachedIn(viewModelScope)
        }else{
            updatedNetSearchResult!!.cachedIn(viewModelScope)
        }
    }

    /*
    * 搜索页点击搜索
    * */
    fun searchScriptByKeyPage(keyword: String) {
        val resultDataFlow = Pager(PagingConfig(pageSize = 10, initialLoadSize =20,enablePlaceholders = true, prefetchDistance = 3)) {
            ScriptNetDataSource(
                RemoteApi.searchDownRetrofit
            )
        }
    }

    fun downScriptByScriptId(scriptInfo : ScriptInfo) {
        this.viewModelScope.launch {
            appDb?.scriptInfoDao?.insert(scriptInfo)
            downScriptSetByScriptId(scriptInfo.script_id)
        }
    }
    fun downScriptSetByScriptId(script_id: Int) {
        this.viewModelScope.launch {
            val result = RemoteApi.searchDownRetrofit.downScriptSetByScriptId(script_id)
            result.data?.let { scriptSetInfoList ->
                scriptSetInfoList.forEach {
                    appDb?.scriptSetInfoDao?.insert(it)
                }
            }
            val re = appDb?.scriptSetInfoDao?.queryScriptSetInfo(script_id)
            re?.let { scriptSetInfoList ->
                scriptSetInfoList.forEach {
                    println("""script_id: ${it.script_id}, script_name: ${it.set_name}, script_url: ${it.set_desc}""")
                }
            }
        }
    }
}