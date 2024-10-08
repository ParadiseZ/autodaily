package com.smart.autodaily.data.entity

import com.google.gson.annotations.SerializedName

data class VersionInfo(
    @SerializedName("script_id") val scriptId : Int,
    @SerializedName("script_version") val scriptVersion: Int,
    @SerializedName("need_app_update") val needAppUpdate : Int,
)
