package com.smart.autodaily.viewmodel.person

import android.app.Application
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.smart.autodaily.api.RemoteApi
import com.smart.autodaily.base.BaseViewModel
import com.smart.autodaily.data.dataresource.CoinRecordDataSource
import com.smart.autodaily.data.entity.VirtualCoinRecord
import com.smart.autodaily.utils.PageUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest

class CoinRecordViewModel(app: Application) : BaseViewModel(application = app) {
    private val _virtualCoinRecordList = MutableStateFlow<PagingData<VirtualCoinRecord>>(PagingData.empty())
    val virtualcoinRecordList : StateFlow<PagingData<VirtualCoinRecord>> get()= _virtualCoinRecordList

    suspend fun getVirtualCoinRecord(){
        appViewModel.user.value?:let {
            appViewModel.loadUserInfo()
        }
        Pager(PagingConfig(pageSize = PageUtil.PAGE_SIZE, initialLoadSize =PageUtil.INITIALOAD_SIZE, prefetchDistance = PageUtil.PREFETCH_DISTANCE)) {
            CoinRecordDataSource(
                RemoteApi.virtualCoinApi,
                appViewModel.user.value!!.userId
            )
        }.flow.collectLatest {
            _virtualCoinRecordList.value = it
        }
    }
}