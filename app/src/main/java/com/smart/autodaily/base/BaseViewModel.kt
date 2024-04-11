package com.smart.autodaily.base

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.smart.autodaily.App

open class BaseViewModel(application: Application) : AndroidViewModel(application) {
    val context: Context by lazy { this.getApplication<App>() }
}