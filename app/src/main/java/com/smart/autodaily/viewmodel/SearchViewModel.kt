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
import com.smart.autodaily.constant.MODEL_BIN
import com.smart.autodaily.constant.MODEL_PARAM
import com.smart.autodaily.data.appDb
import com.smart.autodaily.data.dataresource.ScriptNetDataSource
import com.smart.autodaily.data.entity.DownloadState
import com.smart.autodaily.data.entity.ScriptInfo
import com.smart.autodaily.data.entity.ScriptSetInfo
import com.smart.autodaily.data.entity.resp.Response
import com.smart.autodaily.utils.DownloadManager
import com.smart.autodaily.utils.PageUtil
import com.smart.autodaily.utils.deleteFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import splitties.init.appCtx
import java.io.File
import java.util.Date

class SearchViewModel(application: Application)   : BaseViewModel(application = application)  {
    //远程数据
    private val _remoteScriptList = MutableStateFlow<PagingData<ScriptInfo>>(PagingData.empty())
    val remoteScriptList: StateFlow<PagingData<ScriptInfo>> = _remoteScriptList
    //搜索文本
    private val _searchText = MutableStateFlow("")
    //加载远程数据标志
    private val _loadDataFlagFlow = MutableStateFlow(false)
    val loadDataFlagFlow: StateFlow<Boolean> get() = _loadDataFlagFlow
    /*
    *处理搜索文本
    * */
    fun changeSearchText(key : String){
        this._searchText.value = key
    }
    /*
    * 搜索脚本信息，并和数据库比对，更新flow流以更新is_downloaded标志
    * */
    suspend fun getRemoteScriptList(localList : List<ScriptInfo>) {
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
        val netSearchResult = Pager(PagingConfig(pageSize = PageUtil.PAGE_SIZE, initialLoadSize =PageUtil.INITIALOAD_SIZE, prefetchDistance = PageUtil.PREFETCH_DISTANCE)) {
            ScriptNetDataSource(
                RemoteApi.searchDownRetrofit,
                _searchText.value
            )
        }
        val updatedNetSearchResult : Flow<PagingData<ScriptInfo>> = netSearchResult.flow.map { pagingData ->
            pagingData.map{ scriptInfo ->
                if (scriptInfo.scriptId in localList.map { it.scriptId }){
                    scriptInfo.isDownloaded = 1
                }
                scriptInfo.process = mutableIntStateOf(0)
                scriptInfo
            }
        }
        return updatedNetSearchResult.cachedIn(viewModelScope)
    }

    /*
    * 获取本地脚本信息
    * */


    /*
    * 下载脚本信息，以及脚本设置信息。？img信息、action信息
    * */
    fun downScriptByScriptId(scriptInfo : ScriptInfo) {
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                downloadModel(scriptInfo)
                downByScriptId(scriptInfo)
            }
        }
    }

    private suspend fun downloadModel(scriptInfo : ScriptInfo){
        val externalParamFile = File(appCtx.getExternalFilesDir("") , MODEL_PARAM)
        deleteFile(externalParamFile)
        DownloadManager.download(scriptInfo.scriptId, externalParamFile,"param").collect{
            when (it) {
                is DownloadState.InProgress -> {
                }
                is DownloadState.Success -> {
                }
                is DownloadState.Error -> {
                    deleteFile(externalParamFile)
                    return@collect
                }
            }
        }
        val externalBinFile = File(appCtx.getExternalFilesDir("") , MODEL_BIN)
        deleteFile(externalBinFile)
        DownloadManager.download(scriptInfo.scriptId, externalBinFile,"bin").collect{
            when (it) {
                is DownloadState.InProgress -> {
                    scriptInfo.process.intValue = it.progress
                }
                is DownloadState.Success -> {
                }
                is DownloadState.Error -> {
                    deleteFile(externalBinFile)
                }
            }
        }
    }

    private suspend fun downByScriptId(scriptInfo: ScriptInfo) {
        val result = RemoteApi.searchDownRetrofit.downScriptSetByScriptId(scriptInfo.scriptId)
        //val actionInfo = RemoteApi.searchDownRetrofit.downloadActionInfoByScriptId(scriptInfo.scriptId)
        var globalScriptSetResult = Response<List<ScriptSetInfo>>()
        val localScriptSetGlobal = appDb?.scriptSetInfoDao?.countScriptSetByScriptId(0)
        if (localScriptSetGlobal == 0) {
            globalScriptSetResult = RemoteApi.searchDownRetrofit.downScriptSetByScriptId(0)
        }
        //val scriptSetDownload = RemoteApi.searchDownRetrofit.downScriptSetByScriptId(scriptId)
        appDb?.runInTransaction{
            //scriptInfo
            scriptInfo.isDownloaded = 1
            scriptInfo.scriptVersion = scriptInfo.lastVersion
            scriptInfo.downloadTime = Date().toString()
            appDb?.scriptInfoDao?.insert(scriptInfo)
            //ScriptSet 全局设置
            globalScriptSetResult.data?.let {
                appDb?.scriptSetInfoDao?.insert(it)
            }
            //scriptSetDownload.data?.let {
                //appDb?.scriptSetInfoDao?.insert(it)
           // }
            //ScriptSet 设置
            result.data?.let {
                /*it.map{ ssi->

                }*/
                appDb?.scriptSetInfoDao?.insert(it)
            }
            //picInfo 图片信息
            //actionInfo 动作信息
            /*actionInfo.data?.let {
                appDb?.scriptActionInfoDao?.insert(it)
            }*/
            scriptInfo.process.intValue = -1
        }
    }
}