package com.smart.autodaily.feature.scripting.data.service

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.smart.autodaily.data.entity.DetectResult
import com.smart.autodaily.navpkg.AutoDaily // The actual model class
import com.smart.autodaily.feature.scripting.domain.service.ModelDetectionService
import splitties.init.appCtx // For app context to load files

class ModelDetectionServiceImpl(
    private val context: Context = appCtx, // Injecting context
    private val model: AutoDaily = AutoDaily() // Instantiating the model
) : ModelDetectionService {

    companion object {
        private const val TAG = "ModelDetectionService"
    }

    override suspend fun loadModel(
        modelPath: String, // This is likely the directory name like "yolov8n"
        paramPath: String, // Relative path from modelPath e.g., "model.param"
        binPath: String,   // Relative path from modelPath e.g., "model.bin"
        imgSize: Int,
        useGpu: Boolean,
        cpuThreadNum: Int,
        cpuPower: Int,
        enableNHWC: Boolean,
        enableDebug: Boolean
    ): Boolean {
        val externalFilesDir = context.getExternalFilesDir("")
        if (externalFilesDir == null) {
            Log.e(TAG, "Failed to get external files directory. Cannot load model.")
            return false
        }
        
        // Construct full paths. The modelPath is the directory.
        val fullParamPath = "${externalFilesDir.path}/$modelPath/$paramPath"
        val fullBinPath = "${externalFilesDir.path}/$modelPath/$binPath"

        Log.d(TAG, "Attempting to load model with param: $fullParamPath, bin: $fullBinPath")

        // The AutoDaily.loadModelSec method in the provided snippet for RunScript.kt:
        // fun loadModelSec(assetManager: AssetManager, paramPath: String, binPath: String, imgSize: Int, useGpu:Boolean, cpuThreadNum:Int, cpuPower:Int, enableNHWC:Boolean,enableDebug:Boolean):Boolean
        // This signature expects absolute paths for paramPath and binPath, and AssetManager.
        // This matches our construction of fullParamPath and fullBinPath.
        return try {
            val success = model.loadModelSec(
                context.assets, // AssetManager from context
                fullParamPath,  // Absolute path to .param file
                fullBinPath,    // Absolute path to .bin file
                imgSize,
                useGpu,
                cpuThreadNum,
                cpuPower,
                enableNHWC,
                enableDebug
            )
            if (success) {
                Log.i(TAG, "Model loaded successfully from $modelPath")
            } else {
                Log.e(TAG, "Failed to load model from $modelPath. Check paths and model integrity.")
            }
            success
        } catch (e: Exception) {
            Log.e(TAG, "Exception during model loading from $modelPath: ${e.message}", e)
            false
        }
    }

    override suspend fun detectObjects(bitmap: Bitmap, classesNum: Int): List<DetectResult> {
        return try {
            // The original code filtered nulls: model.detectYolo(capture, si.classesNum).filter { it!=null }
            // Assuming detectYolo can return null or a list that might contain nulls.
            // The ?.filterNotNull() handles both cases gracefully.
            val results = model.detectYolo(bitmap, classesNum)
            results?.filterNotNull() ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Exception during object detection: ${e.message}", e)
            emptyList() // Return empty list on error
        }
    }
}
