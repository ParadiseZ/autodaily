package com.smart.autodaily.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.smart.autodaily.api.SearchDownloadApi
import com.smart.autodaily.data.dataresource.ScriptNetDataSource
import com.smart.autodaily.data.entity.ScriptInfo
import kotlinx.coroutines.flow.Flow

object ScriptNetRepository {
    private const val PAGE_SIZE = 5
    private var netWork = SearchDownloadApi.create()

    fun getPagingData(): Flow<PagingData<ScriptInfo>> {
        return Pager(
            config = PagingConfig(PAGE_SIZE),
            pagingSourceFactory = { ScriptNetDataSource(netWork) }
        ).flow
    }
}