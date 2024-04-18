package com.smart.autodaily.data.dataresource

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.smart.autodaily.data.appDb
import com.smart.autodaily.data.entity.ScriptInfo
import retrofit2.HttpException
import java.io.IOException

class ScriptLocalDataSource : PagingSource<Int, ScriptInfo>() {
     override fun getRefreshKey(state: PagingState<Int, ScriptInfo>): Int? {
         return null
     }
     override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ScriptInfo> {
         return try {
             val page = params.key ?: 1
             val pageSize = params.loadSize
             val startIndex = if (page > 1) pageSize * (page - 1) else 0
             val response = appDb!!.scriptInfoDao.getScriptInfoByPage(pageSize, startIndex)
             LoadResult.Page(
                 data = response,
                 prevKey =  if (page > 1) page - 1 else null,
                 nextKey = if (response.isNotEmpty())  page + 1 else null
             )
         } catch (e: HttpException) {
             Log.e("ScriptLocalDataSource",e.message.toString())
             LoadResult.Error(e)
         }catch (e: IOException) {
             Log.e("ScriptLocalDataSource",e.message.toString())
             LoadResult.Error(e)
         }
     }
}