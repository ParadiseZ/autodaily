package com.smart.autodaily.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.smart.autodaily.api.SearchDownloadApi
import com.smart.autodaily.data.appDb
import com.smart.autodaily.data.dataresource.ScriptNetDataSource
import com.smart.autodaily.data.entity.ScriptSetInfo
import com.smart.autodaily.retrofit2.RetrofitCreate
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel()  {
    var refreshing  =  mutableStateOf(false)
    private val retrofit = RetrofitCreate.create<SearchDownloadApi>()
    private var downScriptSetDate = emptyList<ScriptSetInfo>()
    val resultDataFlow = Pager(PagingConfig(pageSize = 10, initialLoadSize =20,enablePlaceholders = true, prefetchDistance = 3)) {
        ScriptNetDataSource(
            retrofit
        )
    }.flow.cachedIn(viewModelScope)

    fun searchScriptByPage(keyword: String) {
        val resultDataFlow = Pager(PagingConfig(pageSize = 10, initialLoadSize =20,enablePlaceholders = true, prefetchDistance = 3)) {
            ScriptNetDataSource(
                retrofit
            )
        }
    }

    fun downScriptSetByScriptId(script_id: Int) {
        this.viewModelScope.launch {
            val result = retrofit.downScriptSetByScriptId(script_id)
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