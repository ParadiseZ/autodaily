package com.smart.autodaily.data.dataresource

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.smart.autodaily.data.appDb
import com.smart.autodaily.data.entity.ScriptInfo
import com.smart.autodaily.utils.PageUtil
import retrofit2.HttpException
import java.io.IOException

class ScriptLocalDataSource : PagingSource<Int, ScriptInfo>() {
     override fun getRefreshKey(state: PagingState<Int, ScriptInfo>): Int? {
         return null
     }
     override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ScriptInfo> {
         return try {
             val page = params.key ?: PageUtil.FIRST_PAGE
             val pageSize = params.loadSize
             val startIndex = PageUtil.dataStartIndex(page, pageSize)
             val response = appDb.scriptInfoDao.getScriptInfoByPage(pageSize, startIndex)
             LoadResult.Page(
                 data = response,
                 prevKey =  PageUtil.prevKey(page),
                 nextKey = PageUtil.nextKey(page, response)
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