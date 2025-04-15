package com.smart.autodaily.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smart.autodaily.api.RemoteApi
import com.smart.autodaily.constant.ResponseCode
import com.smart.autodaily.data.entity.AppInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import splitties.init.appCtx

class UpdateViewModel : ViewModel() {

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Initial)
    val updateState: StateFlow<UpdateState> = _updateState

    fun checkUpdate() {
        viewModelScope.launch {
            runCatching {
                // 获取当前应用版本信息
                val packageInfo = appCtx.packageManager.getPackageInfo(appCtx.packageName, 0)
                val currentVersionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    packageInfo.longVersionCode.toInt()
                } else {
                    packageInfo.versionCode
                }

                // 调用API获取最新版本信息
                val response = RemoteApi.updateRetrofit.getAppNewVer()

                if (response.code== ResponseCode.SUCCESS.code) {
                    val date = response.data
                    date?.let { list->
                        if (
                            list.first {
                                it.confDesc == "APP_VERSION_CODE"
                            }.confValue.toInt() > currentVersionCode){

                            _updateState.value = UpdateState.HasUpdate(list)
                        }
                    }
                }
            }

        }
    }
    fun closeDialog(){
        _updateState.value = UpdateState.Initial
    }
}

sealed class UpdateState {
    object Initial : UpdateState()
    data class HasUpdate(val updateInfo: List<AppInfo>) : UpdateState()
    data class Error(val message: String) : UpdateState()
}