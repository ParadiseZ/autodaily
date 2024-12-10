package com.smart.autodaily.data.entity

import com.google.gson.annotations.SerializedName

data class ServerConfig(
    @SerializedName("id") val id : Int,
    @SerializedName("config_name") val configName : String,
    @SerializedName("config_value") var configValue : String,
    @SerializedName("config_desc") val configDesc : String
)
