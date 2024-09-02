package com.smart.autodaily

import android.os.Bundle
import androidx.activity.ComponentActivity

class LicenseShowActivity  : ComponentActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.let {
            val type = it.getString("TYPE")
            println(type)
            var fileName = ""
            if(type == "PRIVACY"){
                fileName = "privacy.html"
            }else if(type == "TERMS_OF_USE"){
                fileName = "terms.html"
            }
        }  ?: {
            finish()
        }
    }
}