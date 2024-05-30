package com.smart.autodaily.data.entity.resp

import com.google.gson.annotations.SerializedName
import com.smart.autodaily.constant.ResponseCode

class Response<T> {
    @SerializedName("code") var code: Int = 0
    @SerializedName("message") var message: String? = null
    //或用于内测版
    @SerializedName("data") var data: T ?= null
    //正式版不可试用（到期或未激活）
    @SerializedName("official_data") var officialData: T ?= null
    //正式版可试用
    @SerializedName("probation_data") var probationData: T ?= null
    //正式版未到期
    @SerializedName("normal_data") var normalData: T? = null



    companion object     {
        fun <T> success(): Response<T> {
            return Response<T>().apply {
                this.code = ResponseCode.SUCCESS.code
                this.message = ResponseCode.SUCCESS.message
                this.data = null
            }
        }

        fun <T> success(msg: String): Response<T> {
            return Response<T>().apply {
                this.code = ResponseCode.SUCCESS.code
                this.message = msg
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