package com.smart.autodaily.data.dataresource

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.smart.autodaily.api.KeyTypeApi
import com.smart.autodaily.data.entity.KeyTypeExchange
import com.smart.autodaily.utils.PageUtil
import retrofit2.HttpException
import java.io.IOException

class KeyTypeDataSource (private val kta: KeyTypeApi): PagingSource<Int, KeyTypeExchange>() {
    override fun getRefreshKey(state: PagingState<Int, KeyTypeExchange>): Int? {
        //返回null表示刷新时从第一页开始
        return null
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, KeyTypeExchange> {

        return try {
            val page = params.key ?: PageUtil.FIRST_PAGE
            val pageSize = params.loadSize
            val response =  kta.getKeyTypeList(page, pageSize)
            LoadResult.Page(
                data = response.data!!,
                prevKey =  PageUtil.prevKey(page),
                nextKey = PageUtil.nextKey(page, response.data!!)
            )
        } catch (e: HttpException) {
            Log.e("KeyTypeDataSource",e.message.toString())
            LoadResult.Error(e)
        }catch (e: IOException) {
            Log.e("KeyTypeDataSource",e.message.toString())
            LoadResult.Error(e)
        }
    }
}