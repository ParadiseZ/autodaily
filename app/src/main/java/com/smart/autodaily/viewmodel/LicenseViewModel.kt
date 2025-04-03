package com.smart.autodaily.viewmodel

import android.app.Application
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.viewModelScope
import com.smart.autodaily.base.BaseViewModel
import com.smart.autodaily.data.DataStoreModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch

class LicenseViewModel (app: Application) : BaseViewModel(app){
    private val _hasAccept = MutableStateFlow<Boolean>(false)

    val hasAccept : StateFlow<Boolean> = _hasAccept

    init {
        viewModelScope.launch {
            getPrivacyRes()
        }
    }

    suspend fun getPrivacyRes(){
        val privacy = DataStoreModule.getDataStore().data.map {
            it[booleanPreferencesKey("PRIVACY")]
        }
        val termsUse = DataStoreModule.getDataStore().data.map {
            it[booleanPreferencesKey("TERMS_OF_USE")]
        }
        merge(privacy,termsUse).collectLatest {
            _hasAccept.value = it == true
        }
    }

    suspend fun putPrivacyRes(type : String,flag: Boolean){
        DataStoreModule.getDataStore().edit {
            it[booleanPreferencesKey(type)] = flag
        }
    }
}