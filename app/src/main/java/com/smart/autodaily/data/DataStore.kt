package com.smart.autodaily.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import splitties.init.appCtx


private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auto_daily_preferences")

object DataStoreModule {
    fun getDataStore(): DataStore<Preferences> {
        return appCtx.dataStore
    }
}