package com.smart.autodaily.utils

import com.smart.autodaily.api.RemoteApi
import com.smart.autodaily.data.appDb

object UpdateUtil{
    suspend fun checkScriptUpdate(){
        try {
            // 获取本地脚本列表，并转成ID和版本号的Map
            val localScriptListMap = appDb!!.scriptInfoDao.getIdAndVersion()
                .associate { it.scriptId to it.scriptVersion }
            if (localScriptListMap.isNotEmpty()){
                // 调用远程接口检查更新
                val remoteScriptList = RemoteApi.searchDownRetrofit.checkUpdateByIdAndVer(localScriptListMap)
                // 更新本地数据库中的脚本版本信息
                remoteScriptList.data?.forEach {
                    appDb!!.scriptInfoDao.updateLastVerById(it.key, it.value.scriptVersion ,it.value.needAppUpdate)
                }
                // 重新加载本地数据
            }
        }catch (e:Exception){
            e.message
        }
    }
}