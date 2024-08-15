package com.smart.autodaily.ui.conponent

import android.graphics.PixelFormat
import android.graphics.RectF
import android.os.Build
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.AndroidUiDispatcher
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.compositionContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.smart.autodaily.base.MyComposeViewLifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import splitties.init.appCtx
import splitties.systemservices.windowManager

var detectRectFList : ArrayList<RectF> = arrayListOf()
var imgLayout = mutableStateOf<ImageBitmap?>(null)
val layoutParams = WindowManager.LayoutParams()
var floatingView : ComposeView ?= null

fun initAlertWindow(width: Int, height: Int){
    layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT
    layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
    layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
    layoutParams.format = PixelFormat.TRANSPARENT
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O ){
        layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
    }else{
        layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
    }
    layoutParams.apply {
        gravity = Gravity.TOP or Gravity.START
        x = 0
        y = 0
    }
    floatingView = ComposeView(appCtx).apply {
        setContent {
            OverlayWindow(width,height)
        }
    }
    floatingView!!.addToLifecycle()
    windowManager.addView(floatingView, layoutParams)
}

fun closeWindow(){
    windowManager.removeView(floatingView)
}

@Composable
fun OverlayWindow(width: Int, height: Int) {
    val bitmap by remember {
        imgLayout
    }
    Box (
        modifier = Modifier
            .size(width.dp, height.dp)
            .background(Color.Gray)
    ){
        Text(text = "alterBoxTest")
        bitmap?.let {
            Image(bitmap =it , contentDescription = "null")
        }
    }
}

fun ComposeView.addToLifecycle() {
    val lifecycleOwner = MyComposeViewLifecycleOwner()
    lifecycleOwner.onCreate()
    this.setViewTreeLifecycleOwner(lifecycleOwner)
    this.setViewTreeViewModelStoreOwner(lifecycleOwner)
    this.setViewTreeSavedStateRegistryOwner(lifecycleOwner)
    val coroutineContext = AndroidUiDispatcher.Main
    val runRecomposeScope = CoroutineScope(coroutineContext)
    val reComposer = Recomposer(coroutineContext)
    this.compositionContext = reComposer
    runRecomposeScope.launch {
        reComposer.runRecomposeAndApplyChanges()
    }
}