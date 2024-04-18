package com.smart.autodaily.data.dataresource

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.smart.autodaily.api.SearchDownloadApi
import com.smart.autodaily.data.entity.ScriptInfo
import retrofit2.HttpException
import java.io.IOException

open class ScriptNetDataSource (private val sda: SearchDownloadApi): PagingSource<Int, ScriptInfo>() {
    override fun getRefreshKey(state: PagingState<Int, ScriptInfo>): Int? {
        //返回null表示刷新时从第一页开始
        return null
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ScriptInfo> {

        return try {
            val page = params.key ?: 1
            val pageSize = params.loadSize
            val response =sda.getAllScriptByPage("181" , page, pageSize)
            LoadResult.Page(
                data = response.data,
                prevKey =  if (page > 1) page - 1 else null,
                nextKey = if (response.data.isNotEmpty())  page + 1 else null
                //nextKey = response.data.size+1
            )
        } catch (e: HttpException) {
            Log.e("ScriptNetDataSource",e.message.toString())
            LoadResult.Error(e)
        }catch (e: IOException) {
            Log.e("ScriptNetDataSource",e.message.toString())
            LoadResult.Error(e)
        }
    }
}