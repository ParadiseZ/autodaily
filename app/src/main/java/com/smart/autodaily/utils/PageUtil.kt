package com.smart.autodaily.utils

object PageUtil {
    const val PAGE_SIZE = 5
    const val FIRST_PAGE = 1
    const val INITIALOAD_SIZE = PAGE_SIZE * 3
    const val PREFETCH_DISTANCE = 3
    fun dataStartIndex(page: Int, pageSize: Int) : Int {
        return if (page > 1) pageSize * (page - 1) else 0
    }

    fun prevKey(page: Int) : Int? {
        return if (page > 1) page - 1 else null
    }

    fun <T> nextKey(page: Int, data : List<T>  ) : Int? {
        return if (data.isNotEmpty())  {
            if (page == 1) {
                INITIALOAD_SIZE / PAGE_SIZE + 1
            }else {
                page + 1
            }
        } else null
    }
}