package com.smart.autodaily.utils

import com.smart.autodaily.data.entity.resp.Response
import java.io.IOException

object ExceptionUtil {
    suspend fun <T> tryCatch(tryFun: suspend () -> Response<T>, exceptionMsg :String) : Response<T>{
        try {
            val result  = tryFun()
            if(result.code == 200){
                return result.data?.let {Response.success(it) } ?: Response.success("数据为空！")
            }else{
                return result.message?.let { Response.error(it)} ?: Response.error("服务器未知异常！")
            }
        }catch (e: IOException) {
            return Response.error("网络异常，${exceptionMsg}")
        }catch (e: Exception){
            return Response.error("未知异常，${exceptionMsg}")
        }
    }

    fun <T> tryCatchList(tryFun: Response<List<T>>, exceptionMsg:String) : Response<List<T>>{
        try {
            if(tryFun.code == 200){
                return tryFun.data?.let { Response.success(it) } ?: Response.success("数据为空！")
            }else{
                return tryFun.message?.let { Response.error(it) }
                    ?: Response.error("服务器未知异常！")
            }
        }catch (e: IOException) {
            return Response.error("网络异常，${exceptionMsg}")
        }catch (e: Exception){
            return Response.error("未知异常，${exceptionMsg}")
        }
    }

    fun <T> tryCatch(tryFun: Response<T>, exceptionMsg:String) : Response<T>{
        try {
            if(tryFun.code == 200){
                return tryFun.data?.let { Response.success(it) } ?: Response.success("数据为空！")
            }else{
                return tryFun.message?.let { Response.error(it) }
                    ?: Response.error("服务器未知异常！")
            }
        }catch (e: IOException) {
            return Response.error("网络异常，${exceptionMsg}")
        }catch (e: Exception){
            return Response.error("未知异常，${exceptionMsg}")
        }
    }
}

interface HasData<T> {
    val data: T
}
interface HasCode {
    val code: Int
}
interface HasMessage {
    val message: String
}