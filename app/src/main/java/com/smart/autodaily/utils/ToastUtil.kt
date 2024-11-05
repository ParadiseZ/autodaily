package com.smart.autodaily.utils

import android.content.Context
import android.os.Build
import android.widget.Toast
import com.smart.autodaily.handler.runOnUI

private var toast: Toast? = null

fun Context.toastOnUi(message: Int, duration: Int = Toast.LENGTH_SHORT) {
    toastOnUi(getString(message), duration)
}

fun Context.toastOnUi(message: CharSequence?, duration: Int = Toast.LENGTH_SHORT) {
    when{
        Build.VERSION.SDK_INT <= Build.VERSION_CODES.P ->{
            ToastUtil.show(this, message.toString(), duration)
        }
        else->{
            runOnUI {
                kotlin.runCatching {
                    toast?.cancel()
                    toast = Toast(this)
                    toast?.setText(message)
                    toast?.duration = duration
                    toast?.show()
                }
            }
        }
    }

}
object ToastUtil {
    fun show(context: Context, message: String ,duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, message, duration).show()
    }

    fun showLong(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}