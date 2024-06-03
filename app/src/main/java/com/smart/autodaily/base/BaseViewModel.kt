package com.smart.autodaily.base

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import com.smart.autodaily.App
import com.smart.autodaily.viewmodel.AppViewModel

open class BaseViewModel(application: Application) : AndroidViewModel(application) {
    val context: Context by lazy { this.getApplication<App>() }
    val appViewModel : AppViewModel by lazy {
        ViewModelProvider.AndroidViewModelFactory.getInstance(this.getApplication()).create(AppViewModel::class.java)
    }
}