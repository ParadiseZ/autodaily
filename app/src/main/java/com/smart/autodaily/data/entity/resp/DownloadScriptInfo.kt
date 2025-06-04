package com.smart.autodaily.data.entity.resp

import com.google.gson.annotations.SerializedName
import com.smart.autodaily.data.entity.ScriptInfo

data class DownloadScriptInfo(
    @SerializedName("scriptInfo") var scriptInfo: ScriptInfo,
    @SerializedName("paramMd5") var paramMd5: String?,
    @SerializedName("binMd5") var binMd5: String?
)