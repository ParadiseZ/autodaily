package com.smart.autodaily.data.dataresource

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.smart.autodaily.data.entity.ScriptInfo
import com.smart.autodaily.data.repository.ScriptNetRepository
import retrofit2.HttpException
import java.io.IOException

open class ScriptNetDataSource (private val sda: ScriptNetRepository): PagingSource<Int, ScriptInfo>() {
    override fun getRefreshKey(state: PagingState<Int, ScriptInfo>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ScriptInfo> {

        return try {
            val page = params.key ?: 1 // set page 1 as default
            val pageSize = params.loadSize // set page size 10 as default
            //val response = sda.getAllScriptByPage("test",page, pageSize)
            val response =sda.getScriptByPage("181")
            //al response = sda.getProjects(page, 294)
            println("response___"+response.data.toString())
            LoadResult.Page(
                data = response.data,
                prevKey = if (page > 1) page - 1 else null,
                //nextKey = if(response.data.isNotEmpty()) page+1 else null
                nextKey =  null
                /*data = response.data.datas,
                prevKey = if(page==1) null else page-1,
                nextKey = if(page<response.data.pageCount) page+1 else null*/
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