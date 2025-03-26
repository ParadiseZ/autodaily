package com.smart.autodaily.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import splitties.init.appCtx

object TokenManager {
    private const val PREF_NAME = "TokenPrefs"
    private const val KEY_TOKEN = "auth_token"
    
    private val prefs: SharedPreferences = appCtx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    
    fun saveToken(token: String) {
        prefs.edit { putString(KEY_TOKEN, token) }
    }
    
    fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }
    
    fun clearToken() {
        prefs.edit() { remove(KEY_TOKEN) }
    }
} 