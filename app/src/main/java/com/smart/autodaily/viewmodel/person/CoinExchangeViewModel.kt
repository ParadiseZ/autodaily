package com.smart.autodaily.viewmodel.person

import android.app.Application
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.smart.autodaily.api.RemoteApi
import com.smart.autodaily.base.BaseViewModel
import com.smart.autodaily.constant.ResponseCode
import com.smart.autodaily.data.appDb
import com.smart.autodaily.data.dataresource.KeyTypeDataSource
import com.smart.autodaily.data.entity.KeyTypeExchange
import com.smart.autodaily.data.entity.UserInfo
import com.smart.autodaily.data.entity.resp.Response
import com.smart.autodaily.utils.PageUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest

class CoinExchangeViewModel (app: Application) : BaseViewModel(application = app) {
    private val _keyTypeList  = MutableStateFlow<PagingData<KeyTypeExchange>>(PagingData.empty())
    val keyTypeList : StateFlow<PagingData<KeyTypeExchange>> get() = _keyTypeList

    suspend fun getKeyTypeList(){
        Pager(PagingConfig(pageSize = PageUtil.PAGE_SIZE, initialLoadSize = PageUtil.INITIALOAD_SIZE, prefetchDistance = PageUtil.PREFETCH_DISTANCE)) {
            KeyTypeDataSource(
                RemoteApi.keyTypeApi
            )
        }.flow.collectLatest {
            _keyTypeList.value = it
        }
    }

    suspend fun exchangeVip(keyTypeId : Int)  : Response<UserInfo>?{
        var res: Response<UserInfo>? = null
        appViewModel.user.value?:let{
            appViewModel.loadUserInfo()
        }
        appViewModel.user.value?.userId?.let {
            res = RemoteApi.virtualCoinApi.exchangeVip(it, keyTypeId)
            if (res!!.code == ResponseCode.SUCCESS.code) {
                appDb.runInTransaction{
                    appViewModel.updateUser(res!!.data)
                }
            }
        }
        return res
    }
}