package com.smart.autodaily.service

import android.media.projection.MediaProjection
import com.smart.autodaily.utils.ScreenCaptureUtil


class MediaProjectionCallback: MediaProjection.Callback (){
    override fun onStop() {
        super.onStop()
        ScreenCaptureUtil.releaseCapture()
    }
}