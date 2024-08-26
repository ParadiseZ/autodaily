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

static cv::Mat bitmapToMat(JNIEnv *env, jobject j_argb8888_bitmap) {
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

static void matToBitmap(JNIEnv *env, cv::Mat & drawMat, jobject obj_bitmap){
    //锁定画布
    void *pixels;
    AndroidBitmap_lockPixels(env,obj_bitmap,&pixels);
    //获取Bitmap的信息
    AndroidBitmapInfo bitmapInfo;
    AndroidBitmap_getInfo(env,obj_bitmap,&bitmapInfo);
    // 将Mat数据复制到Bitmap
    cv::Mat bitmapMat(bitmapInfo.height, bitmapInfo.width, CV_8UC4, pixels);
    drawMat.copyTo(bitmapMat);
    AndroidBitmap_unlockPixels(env, obj_bitmap);
}

static jobjectArray changeDetectResultToJavaArray(JNIEnv *env, const std::vector<Object>& objects){
    // 3. 将检测结果转换为Java对象数组
    jclass detectionResultClass = env->FindClass("com/smart/autodaily/data/entity/DetectResult");
    jmethodID detectionResultConstructor = env->GetMethodID(detectionResultClass, "<init>",
                                                            "(IFLcom/smart/autodaily/data/entity/Rect;FF)V");

    // 创建Java对象数组
    jobjectArray resultArray = env->NewObjectArray(objects.size(), detectionResultClass, NULL);
    //RectF
    jclass rectFClass = env->FindClass("com/smart/autodaily/data/entity/Rect");
    jmethodID  rectFClassConstructor = env->GetMethodID(rectFClass, "<init>",
                                                        "(FFFF)V");
    for (size_t i = 0; i < objects.size(); ++i) {
        Object obj = objects[i];
        jobject rect = env->NewObject(rectFClass,
                                      rectFClassConstructor,
                                      obj.rect.x, obj.rect.y, obj.rect.width, obj.rect.height);
        jobject detectionResult = env->NewObject(detectionResultClass, detectionResultConstructor,
                                                 obj.label, obj.prob, rect ,(obj.rect.x + obj.rect.width/2), obj.rect.x + obj.rect.width/2);
        env->SetObjectArrayElement(resultArray, i, detectionResult);
    }
    return resultArray;
}
extern "C" {

    JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved)
    {
        __android_log_print(ANDROID_LOG_DEBUG, "ncnn", "JNI_OnLoad");
        return JNI_VERSION_1_6;
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
        // Get the actual characters of the string
        const char* c_str = env -> GetStringUTFChars(modelPath,0);
        //const char* modeltypes[] ={"bh3/cn/model.ncnn"};
        const int target_sizes[] = {targetSize};
        /*const float mean_vals[][3] ={
                {103.53f, 116.28f, 123.675f},
        };*/
        const float norm_vals[][3] ={
                { 1 / 255.f, 1 / 255.f, 1 / 255.f },
        };

        //const char* modeltype = modeltypes[0];
        int target_size = target_sizes[0];

        // reload
        {
            ncnn::MutexLockGuard g(lock);
            if (!g_yolo)
                g_yolo = new Yolo;
            if (ncnn::get_gpu_count() == 0)
            {
                g_yolo->load(mgr, c_str, target_size , norm_vals[0], false);
            }
            else
            {
                g_yolo->load(mgr, c_str, target_size , norm_vals[0], use_gpu);
            }
        }
        env -> ReleaseStringUTFChars(modelPath, c_str);
        return JNI_TRUE;
    }

    JNIEXPORT jobjectArray JNICALL
    Java_com_smart_autodaily_navpkg_AutoDaily_detect
            (JNIEnv *env, jobject thiz, jobject imageData, jint numClasses,
             jfloat threshold, jfloat nmsThreshold) {
        // 1. 从Bitmap转换为cv::Mat
        cv::Mat image =  bitmapToMat(env, imageData);

        // 1.1. 转换颜色空间
        //cv::Mat imageBGR;
        //cv::cvtColor(image, imageBGR, cv::COLOR_RGBA2BGR);

        // 2. 调用detect方法
        std::vector<Object> objects;
        //Yolo detector;
        g_yolo->detect(image, objects,numClasses, threshold, nmsThreshold);
        return changeDetectResultToJavaArray(env, objects);
    }

JNIEXPORT jobjectArray JNICALL
Java_com_smart_autodaily_navpkg_AutoDaily_detectAndDraw
        (JNIEnv *env, jobject thiz, jobject imageData, jint numClasses,
         jfloat threshold, jfloat nmsThreshold, jobject drawBitMap) {
        cv::Mat img =  bitmapToMat(env, imageData);

        std::vector<Object> objects;
        //Yolo detector;
        g_yolo->detect(img, objects,numClasses, threshold, nmsThreshold);
        cv::Mat drawMat = img.clone();
        g_yolo ->draw(img, drawMat, objects);
        matToBitmap(env, drawMat, drawBitMap);
        return changeDetectResultToJavaArray(env, objects);
    }
}