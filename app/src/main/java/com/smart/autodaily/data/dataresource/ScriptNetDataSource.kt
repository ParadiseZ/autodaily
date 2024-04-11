package com.smart.autodaily.data.dataresource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.smart.autodaily.api.SearchDownloadApi
import com.smart.autodaily.data.entity.ScriptInfo

class ScriptNetDataSource (private val sda: SearchDownloadApi): PagingSource<Int, ScriptInfo>() {
    override fun getRefreshKey(state: PagingState<Int, ScriptInfo>): Int? {
        return null
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ScriptInfo> {
        return try {
            val page = params.key ?: 1 // set page 1 as default
            val pageSize = params.loadSize
            val repoResponse = sda.getAllScriptByPage("test",page, pageSize)
            val repoItems = repoResponse.items
            val prevKey = if (page > 1) page - 1 else null
            val nextKey = if (repoItems.isNotEmpty()) page + 1 else null
            LoadResult.Page(repoItems, prevKey, nextKey)
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}