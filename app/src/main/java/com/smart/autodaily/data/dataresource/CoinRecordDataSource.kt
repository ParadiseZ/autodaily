package com.smart.autodaily.data.dataresource

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.smart.autodaily.api.VirtualCoinApi
import com.smart.autodaily.data.entity.VirtualCoinRecord
import com.smart.autodaily.utils.PageUtil
import retrofit2.HttpException
import java.io.IOException

open class CoinRecordDataSource (private val vca: VirtualCoinApi, private val userId: Int): PagingSource<Int, VirtualCoinRecord>() {
    override fun getRefreshKey(state: PagingState<Int, VirtualCoinRecord>): Int? {
        //返回null表示刷新时从第一页开始
        return null
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, VirtualCoinRecord> {

        return try {
            val page = params.key ?: PageUtil.FIRST_PAGE
            val pageSize = params.loadSize
            val response = vca.getCoinRecord(userId, page, pageSize)
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