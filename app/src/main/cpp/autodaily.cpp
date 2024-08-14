// Tencent is pleased to support the open source community by making ncnn available.
//
// Copyright (C) 2021 THL A29 Limited, a Tencent company. All rights reserved.
//
// Licensed under the BSD 3-Clause License (the "License"); you may not use this file except
// in compliance with the License. You may obtain a copy of the License at
//
// https://opensource.org/licenses/BSD-3-Clause
//
// Unless required by applicable law or agreed to in writing, software distributed
// under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
// CONDITIONS OF ANY KIND, either express or implied. See the License for the
// specific language governing permissions and limitations under the License.

#include <android/asset_manager_jni.h>
#include <android/native_window_jni.h>
#include <android/native_window.h>
#include <android/bitmap.h>

#include <android/log.h>

#include <jni.h>

#include <string>
#include <vector>

#include <platform.h>
#include <benchmark.h>

#include "yolo.h"

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>

#if __ARM_NEON
#include <arm_neon.h>
#endif // __ARM_NEON


static Yolo* g_yolo = 0;
static ncnn::Mutex lock;

#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, "ncnn", __VA_ARGS__))

cv::Mat bitmapToMat(JNIEnv *env, jobject j_argb8888_bitmap) {
    cv::Mat c_rgba;
    AndroidBitmapInfo j_bitmap_info;
    if (AndroidBitmap_getInfo(env, j_argb8888_bitmap, &j_bitmap_info) < 0) {
        return c_rgba;
    }
    if (j_bitmap_info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        return c_rgba;
    }
    void *j_bitmap_pixels;
    if (AndroidBitmap_lockPixels(env, j_argb8888_bitmap, &j_bitmap_pixels) < 0) {
        return c_rgba;
    }
    cv::Mat j_bitmap_im(static_cast<int>(j_bitmap_info.height),
                        static_cast<int>(j_bitmap_info.width), CV_8UC4,
                        j_bitmap_pixels); // no copied.
    c_rgba = j_bitmap_im; // ref only.
    if (AndroidBitmap_unlockPixels(env, j_argb8888_bitmap) < 0) {
        return c_rgba;
    }
    return c_rgba;
}


extern "C" {

    JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved)
    {
        __android_log_print(ANDROID_LOG_DEBUG, "ncnn", "JNI_OnLoad");
        return JNI_VERSION_1_4;
    }

    JNIEXPORT void JNI_OnUnload(JavaVM* vm, void* reserved)
    {
        __android_log_print(ANDROID_LOG_DEBUG, "ncnn", "JNI_OnUnload");

        {
            ncnn::MutexLockGuard g(lock);

            delete g_yolo;
            g_yolo = 0;
        }
    }

    // public native boolean loadModel(AssetManager mgr, int modelid, int cpugpu);
    JNIEXPORT jboolean JNICALL Java_com_smart_autodaily_navpkg_AutoDaily_loadModel(JNIEnv* env, jobject thiz, jobject assetManager, jstring modelPath, jint targetSize, jboolean use_gpu)
    {

        AAssetManager* mgr = AAssetManager_fromJava(env, assetManager);

        //__android_log_print(ANDROID_LOG_DEBUG, "ncnn", "loadModel %p", mgr);
        LOGD("loadModel %s", modelPath);

        const char* modeltypes[] =
                {
                        //reinterpret_cast<const char *>(modelPath),
                        "bh3/cn/model.ncnn"
                };

        const int target_sizes[] =
                {
                        targetSize,
                };

        const float mean_vals[][3] =
                {
                        {103.53f, 116.28f, 123.675f},
                };

        const float norm_vals[][3] =
                {
                        { 1 / 255.f, 1 / 255.f, 1 / 255.f },
                };

        const char* modeltype = modeltypes[0];
        int target_size = target_sizes[0];

        // reload
        {
            ncnn::MutexLockGuard g(lock);

            if (use_gpu && ncnn::get_gpu_count() == 0)
            {
                // no gpu
                delete g_yolo;
                g_yolo = 0;
            }
            else
            {
                if (!g_yolo)
                    g_yolo = new Yolo;
                g_yolo->load(mgr, modeltype, target_size, mean_vals[0], norm_vals[0], use_gpu);
            }
        }

        return JNI_TRUE;
    }

    JNIEXPORT jobjectArray JNICALL
    Java_com_smart_autodaily_navpkg_AutoDaily_detect
            (JNIEnv *env, jobject thiz, jobject imageData,
             jfloat threshold, jfloat nmsThreshold) {
        // 1. 从Bitmap转换为cv::Mat
        cv::Mat image =  bitmapToMat(env, imageData);
        LOGD("imgWidth：%d, imgHeight：%d",image.cols, image.rows);
       /* if(!BitmapToMatrix(env, imageData, image)){
            return  env->NewObjectArray(0, NULL, NULL);
        }*/

        // 1.1. 转换颜色空间
        //cv::Mat imageBGR;
        //cv::cvtColor(image, imageBGR, cv::COLOR_RGBA2BGR);

        // 2. 调用detect方法
        std::vector<Object> objects;
        //Yolo detector;
        g_yolo->detect(image, objects, threshold, nmsThreshold);
        LOGD("detect over");
        // 3. 将检测结果转换为Java对象数组
        jclass detectionResultClass = env->FindClass("com/smart/autodaily/data/entity/DetectResult");
        jmethodID detectionResultConstructor = env->GetMethodID(detectionResultClass, "<init>",
                                                                "(IFLandroid/graphics/RectF;)V");

        // 创建Java对象数组
        jobjectArray resultArray = env->NewObjectArray(objects.size(), detectionResultClass, NULL);
        //RectF
        jclass rectFClass = env->FindClass("android/graphics/RectF");
        jmethodID  rectFClassConstructor = env->GetMethodID(rectFClass, "<init>",
                                                            "(FFFF)V");
        for (size_t i = 0; i < objects.size(); ++i) {
            Object obj = objects[i];
            jobject rect = env->NewObject(rectFClass,
            rectFClassConstructor,
                                          obj.rect.x, obj.rect.y, obj.rect.x + obj.rect.width, obj.rect.y + obj.rect.height);
            jobject detectionResult = env->NewObject(detectionResultClass, detectionResultConstructor,
                                                     obj.label, obj.prob, rect);
            env->SetObjectArrayElement(resultArray, i, detectionResult);
        }
        return resultArray;
    }
}