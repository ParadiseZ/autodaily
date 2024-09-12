package com.smart.autodaily.viewmodel.person

import android.app.Application
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.smart.autodaily.api.RemoteApi
import com.smart.autodaily.base.BaseViewModel
import com.smart.autodaily.data.dataresource.UserKeyRecordDataSource
import com.smart.autodaily.data.entity.UserKeyRecord
import com.smart.autodaily.utils.PageUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest

class UserKeyRecordViewModel(app: Application) : BaseViewModel(application = app)  {
    private val _userKeyRecords = MutableStateFlow<PagingData<UserKeyRecord>>(PagingData.empty())
    val userKeyRecords : StateFlow<PagingData<UserKeyRecord>> get()= _userKeyRecords

    suspend fun getUserKeyRecord(){
        Pager(PagingConfig(pageSize = PageUtil.PAGE_SIZE, initialLoadSize = PageUtil.INITIALOAD_SIZE, prefetchDistance = PageUtil.PREFETCH_DISTANCE)) {
            UserKeyRecordDataSource(
                RemoteApi.userKeyRecordApi,
                appViewModel.user.value!!.userId
            )
        }.flow.collectLatest {
            _userKeyRecords.value = it
        }
    }
}