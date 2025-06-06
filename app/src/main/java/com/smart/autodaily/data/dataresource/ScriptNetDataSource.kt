package com.smart.autodaily.data.dataresource

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.smart.autodaily.api.SearchDownloadApi
import com.smart.autodaily.data.entity.ScriptInfo
import com.smart.autodaily.utils.PageUtil
import retrofit2.HttpException
import java.io.IOException

open class ScriptNetDataSource (private val sda: SearchDownloadApi, private val searchKey: String?,var userId : Int): PagingSource<Int, ScriptInfo>() {
    override fun getRefreshKey(state: PagingState<Int, ScriptInfo>): Int? {
        //返回null表示刷新时从第一页开始
        return null
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ScriptInfo> {

        return try {
            val page = params.key ?: PageUtil.FIRST_PAGE
            val response =if (searchKey?.isNotBlank() == true) {
                sda.getAllScriptByPage(userId ,searchKey , page, params.loadSize)
            } else{
                sda.getAllScriptByPage(userId , page, params.loadSize)
            }
            LoadResult.Page(
                data = response.data!!,
                prevKey =  PageUtil.prevKey(page),
                nextKey = PageUtil.nextKey(page, response.data!!)
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