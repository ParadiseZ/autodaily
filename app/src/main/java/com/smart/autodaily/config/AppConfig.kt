package com.smart.autodaily.config

import android.content.SharedPreferences

object AppConfig : SharedPreferences.OnSharedPreferenceChangeListener {
    const val channelIdDownload = "channel_download"
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        //优化监听sharePreference
    }
}