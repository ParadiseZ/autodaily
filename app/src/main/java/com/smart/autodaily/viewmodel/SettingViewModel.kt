package com.smart.autodaily.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.smart.autodaily.base.BaseViewModel
import com.smart.autodaily.data.appDb
import com.smart.autodaily.data.dataresource.ScriptSetLocalDataSource
import com.smart.autodaily.data.entity.ScriptRunStatus
import com.smart.autodaily.data.entity.ScriptSetInfo
import com.smart.autodaily.utils.PageUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate

val mediaProjectionServiceStartFlag by lazy {
    mutableStateOf(false)// 全局变量，用于控制媒体投影服务是否启动
}
class SettingViewModel (app: Application) : BaseViewModel(application = app) {

    private val _canUpdate  = mutableStateOf(false)

    fun getGlobalSetting(): Flow<PagingData<ScriptSetInfo>> {
        return Pager(
            PagingConfig(
                pageSize = PageUtil.PAGE_SIZE,
                initialLoadSize = PageUtil.INITIALOAD_SIZE,
                prefetchDistance = PageUtil.PREFETCH_DISTANCE
            )
        ) {
            ScriptSetLocalDataSource()
        }.flow.cachedIn(viewModelScope)
    }

    fun updateGlobalSetting(scriptSetInfo: ScriptSetInfo) {
        try {
            appDb.scriptSetInfoDao.update(scriptSetInfo)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun insertStatus(scriptId : Int,flowId : Int){
        viewModelScope.launch {
            val sets = appDb.scriptSetInfoDao.getScriptSetByFlowId(scriptId,flowId)
            for (set in sets) {
                try {
                    appDb.scriptRunStatusDao.insert(
                        ScriptRunStatus(
                            scriptId = scriptId,
                            flowId = flowId,
                            flowIdType = set.flowIdType,
                            curStatus = 2,
                            dateTime = LocalDate.now()
                                .toString()
                        )
                    )
                }catch (e : Exception){
                    println("错误$set")
                }
            }
        }

    }

    fun deleteStatus(scriptId : Int,flowId : Int){
        viewModelScope.launch {
            appDb.scriptRunStatusDao.deleteStatus(scriptId,flowId)
        }
    }

    fun deleteByScriptId(scriptId : Int){
        viewModelScope.launch {
            appDb.scriptRunStatusDao.deleteStatus(scriptId)
        }
    }
}
