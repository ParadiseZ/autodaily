package com.smart.autodaily.data.entity.resp

import com.smart.autodaily.constant.ResponseCode

class Response<T> {
    var code: Int = 0
    var message: String? = null
    var data: T? = null


    companion object     {
        fun <T> success(): Response<T> {
            return Response<T>().apply {
                this.code = ResponseCode.SUCCESS.code
                this.message = ResponseCode.SUCCESS.message
                this.data = null
            }
        }

        fun <T> success(data: T): Response<T> {
            return Response<T>().apply {
                this.code = ResponseCode.SUCCESS.code
                this.message = ResponseCode.SUCCESS.message
                this.data = data
            }
        }

        fun <T> error(code: Int, message: String): Response<T> {
            return Response<T>().apply {
                this.code = code
                this.message = message
                this.data = null
            }
        }

        fun <T> error(message: String): Response<T> {
            return Response<T>().apply {
                this.code = -1
                this.message = message
                this.data = null
            }
        }
    }
}