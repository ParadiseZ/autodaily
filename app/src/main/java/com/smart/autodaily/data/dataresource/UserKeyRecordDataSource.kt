package com.smart.autodaily.data.dataresource

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.smart.autodaily.api.UserKeyRecordApi
import com.smart.autodaily.data.entity.UserKeyRecord
import com.smart.autodaily.utils.PageUtil
import retrofit2.HttpException
import java.io.IOException

class UserKeyRecordDataSource (private val ukr: UserKeyRecordApi, private val userId: Int): PagingSource<Int, UserKeyRecord>() {
    override fun getRefreshKey(state: PagingState<Int, UserKeyRecord>): Int? {
        //返回null表示刷新时从第一页开始
        return null
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, UserKeyRecord> {

        return try {
            val page = params.key ?: PageUtil.FIRST_PAGE
            val pageSize = params.loadSize
            val response = ukr.getInputKeyRecord(userId, page, pageSize)
            LoadResult.Page(
                data = response.data!!,
                prevKey =  PageUtil.prevKey(page),
                nextKey = PageUtil.nextKey(page, response.data!!)
            )
        } catch (e: HttpException) {
            Log.e("UserKeyRecordDataSource",e.message.toString())
            LoadResult.Error(e)
        }catch (e: IOException) {
            Log.e("UserKeyRecordDataSource",e.message.toString())
            LoadResult.Error(e)
        }
    }
}