package com.smart.autodaily.navpkg

import android.content.res.AssetManager
import android.view.Surface

class autodaily{
    external fun loadModel(mgr : AssetManager,modelPath : Char, targetSize: Int, useGpu : Boolean) : Boolean

    external fun detect() : Boolean

    companion object{
        System.loadLibrary("autodaily")
    }
}