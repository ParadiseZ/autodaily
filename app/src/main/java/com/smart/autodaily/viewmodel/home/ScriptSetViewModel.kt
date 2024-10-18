package com.smart.autodaily.viewmodel.home

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.smart.autodaily.base.BaseViewModel
import com.smart.autodaily.data.appDb
import com.smart.autodaily.data.entity.ScriptSetInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ScriptSetViewModel  (application: Application) : BaseViewModel(application = application) {

    private val _scriptSetList = MutableStateFlow<List<ScriptSetInfo>>(emptyList())
    val scriptSetList : StateFlow<List<ScriptSetInfo>> get()= _scriptSetList

    fun getScriptSetById(scriptId : Int) {
        viewModelScope.launch {
            appDb!!.scriptSetInfoDao.getScriptSetByScriptId(scriptId).collectLatest {
                _scriptSetList.value = it
            }
        }
    }

    fun updateScriptSet(scriptSetInfo: ScriptSetInfo){
        viewModelScope.launch {
            try {
                appDb!!.scriptSetInfoDao.update(scriptSetInfo)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}