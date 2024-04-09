package com.smart.autodaily.utils

import android.content.Context
import android.content.res.Resources
import android.widget.Toast
import androidx.compose.runtime.compositionLocalOf

object ScreenUtil {

    fun xPixInt( multiple : Float = 1f ) : Int{
        return ((Resources.getSystem().displayMetrics.widthPixels/Resources.getSystem().displayMetrics.density + 0.5f) * multiple).toInt()
    }

    fun yPixInt( multiple : Float = 1f ) : Int{
        return ((Resources.getSystem().displayMetrics.heightPixels/Resources.getSystem().displayMetrics.density + 0.5f) * multiple).toInt()
        //return Resources.getSystem().displayMetrics.ydpi
        ///return (Resources.getSystem().displayMetrics.heightPixels * multiple).toInt()
    }

    //弹窗
    fun showMsg (message:String, context: Context){
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}