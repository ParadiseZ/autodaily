package com.smart.autodaily.viewmodel

import android.app.Application
import com.smart.autodaily.base.BaseViewModel
import com.smart.autodaily.data.appDb

class LicenseViewModel (app: Application) : BaseViewModel(app){
    fun updateCheckFlag(id : Int, value : Boolean){
        appDb.appInfoDao.updateValueById(id, value)
    }
}