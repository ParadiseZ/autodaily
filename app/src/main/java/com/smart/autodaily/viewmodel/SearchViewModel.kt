package com.smart.autodaily.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.smart.autodaily.data.dataresource.ScriptNetDataSource
import com.smart.autodaily.data.entity.ScriptInfo
import com.smart.autodaily.data.repository.ScriptNetRepository
import kotlinx.coroutines.flow.Flow

class SearchViewModel : ViewModel()  {
    var refreshing  =  mutableStateOf(false)
    
     fun getPagingData(): Flow<PagingData<ScriptInfo>> {
         return Pager(PagingConfig(pageSize = 2)) {
             ScriptNetDataSource(ScriptNetRepository)
         }.flow.cachedIn(viewModelScope)
    }
}