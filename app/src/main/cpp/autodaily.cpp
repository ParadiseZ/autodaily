// Android相关头文件
#include <android/asset_manager_jni.h>
#include <android/bitmap.h>
#include <android/log.h>
// 项目依赖头文件
#include <platform.h>
#include <benchmark.h>
#include <fcntl.h>

// 自定义头文件
#include "yolo.h"
#include "CrnnNet.h"

// ARM NEON优化
#if __ARM_NEON
#include <arm_neon.h>
#endif // __ARM_NEON

static Yolo *g_yolo = nullptr;
static CrnnNet *g_crnn = nullptr;
static bool needOcr  = false;
static ncnn::Mutex lock;
static FILE *paramFile = nullptr;
static FILE *modelFile = nullptr;

static jclass detectResCls = nullptr;
static jmethodID detectResCon;
static jclass rectCls = nullptr;
static jmethodID rectCon;
//ocr
static jclass ocrResCls = nullptr;
static jmethodID ocrResMethod = nullptr;
static jclass hashSetClass = nullptr;
static jclass shortClass = nullptr;
static jmethodID hashSetCon = nullptr;
static jmethodID shortCon = nullptr;
static jmethodID hashAddMethod = nullptr;

bool loadOcrModel(JNIEnv *env,jobject assetManager, jint lang, jboolean useGpu,jshort colorStep,jboolean getColor)
{
    AAssetManager *mgr = AAssetManager_fromJava(env, assetManager);
    // Get the actual characters of the string

    // reload
    const float rec_mean_vals[3] = { 127.5, 127.5, 127.5 };
    const float rec_norm_vals[3] = { 1.0 / 127.5, 1.0 / 127.5, 1.0 / 127.5 };
    {
        ncnn::MutexLockGuard ocr(lock);
        if (!g_crnn)
            g_crnn = new CrnnNet;
#if NCNN_VULKAN
        if (ncnn::get_gpu_count() == 0)
        {
            g_crnn->load(mgr, 48,colorStep, rec_mean_vals, rec_norm_vals, false,lang, getColor);
        }
        else
        {
            g_crnn->load(mgr, 48,colorStep, rec_mean_vals, rec_norm_vals,useGpu,lang, getColor);
        }
#else
        g_crnn->load(mgr, 48,colorStep, rec_mean_vals, rec_norm_vals, false,lang, getColor);
#endif
    }
    return JNI_TRUE;
}
// OCR END
static jobject ocrResHandler(JNIEnv *env, TextLine txt){
    //label
    jobject hashSet = env->NewObject(hashSetClass, hashSetCon);
    jint l = (jint)txt.label.size();
    jshortArray labelArr = env->NewShortArray(l);
    env->SetShortArrayRegion(labelArr, 0, l, txt.label.data());
    //txt
    jstring text = env->NewStringUTF(txt.text.c_str());
    //颜色处理begin
    jobject colorSet = env->NewObject(hashSetClass, hashSetCon);
    jint colorLen = (jint)txt.color.size();
    jshortArray colorArr = env->NewShortArray(colorLen);
    env->SetShortArrayRegion(colorArr, 0, colorLen, txt.color.data());
    //颜色处理end
    jobject res = env->NewObject(ocrResCls, ocrResMethod, hashSet, labelArr, text, colorSet, colorArr);
    // 释放局部引用
    env->DeleteLocalRef(hashSet);
    env->DeleteLocalRef(labelArr);
    env->DeleteLocalRef(text);
    env->DeleteLocalRef(colorSet);
    env->DeleteLocalRef(colorArr);
    return  res;
}
//Detect
static jobjectArray transformResult(JNIEnv *env, const std::vector<Object> &objects, cv::Mat & src)
{
    // 创建Java对象数组
    jobjectArray resultArray = env->NewObjectArray(static_cast<jsize>(objects.size()), detectResCls, nullptr);
    for (size_t i = 0; i < objects.size(); ++i)
    {
        Object obj = objects[i];
        jobject rect = env->NewObject(rectCls,
                                      rectCon,
                                      obj.rect.x, obj.rect.y, obj.rect.width, obj.rect.height);
        jobject ocrRes = nullptr;
        if(obj.label == 0 && needOcr){
            if( g_crnn != nullptr){
                try{
                    TextLine textLine = g_crnn->getTextLine(src,obj.rect);
                    if (textLine.text.empty()){
                        env->DeleteLocalRef(rect);
                        continue;
                    }
                    ocrRes = ocrResHandler(env, textLine);
                }catch(const std::exception& e) {
                    __android_log_print(ANDROID_LOG_ERROR, "CRNN", "ocr eeror at %zu,%s", i ,e.what());
                    continue;
                }
            }
        }
        jobject detectRes = env->NewObject(detectResCls, detectResCon,
                                           obj.label, obj.prob, rect, (obj.rect.x + obj.rect.width / 2), obj.rect.y + obj.rect.height / 2, ocrRes);
        env->SetObjectArrayElement(resultArray, i, detectRes);
        env->DeleteLocalRef(rect);
        env->DeleteLocalRef(ocrRes);
        env->DeleteLocalRef(detectRes);
    }
    return resultArray;
}

extern "C"
{

    JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved)
    {
        __android_log_print(ANDROID_LOG_DEBUG, "ncnn", "JNI_OnLoad");
        #if NCNN_VULKAN
            ncnn::create_gpu_instance();
        #endif
        JNIEnv *env;
        if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK)
        {
            return JNI_ERR;
        }
        // detect
        jclass localDetectResCls = env->FindClass("com/smart/autodaily/data/entity/DetectResult");
        detectResCls = (jclass)env->NewGlobalRef(localDetectResCls);
        detectResCon = env->GetMethodID(detectResCls, "<init>", "(IFLcom/smart/autodaily/data/entity/Rect;FFLcom/smart/autodaily/data/entity/OcrResult;)V");
        env->DeleteLocalRef(localDetectResCls);

        jclass localRectCls = env->FindClass("com/smart/autodaily/data/entity/Rect");
        rectCls = (jclass)env->NewGlobalRef(localRectCls);
        rectCon = env->GetMethodID(rectCls, "<init>", "(FFFF)V");
        env->DeleteLocalRef(localRectCls);

        //OCR
        jclass localOcrResCls = env->FindClass("com/smart/autodaily/data/entity/OcrResult");
        ocrResCls = (jclass)env->NewGlobalRef(localOcrResCls);
        ocrResMethod = env->GetMethodID(ocrResCls, "<init>", "(Ljava/util/Set;[SLjava/lang/String;Ljava/util/Set;[S)V");
        env->DeleteLocalRef(localOcrResCls);

        jclass localHashSetCls = env->FindClass("java/util/HashSet");
        hashSetClass =  (jclass)env->NewGlobalRef(localHashSetCls);
        hashSetCon = env->GetMethodID(hashSetClass, "<init>", "()V");
        hashAddMethod = env->GetMethodID(hashSetClass, "add", "(Ljava/lang/Object;)Z");
        env->DeleteLocalRef(localHashSetCls);

        jclass localShortCls = env->FindClass("java/lang/Short");
        shortClass = (jclass)env->NewGlobalRef(localShortCls);
        shortCon = env->GetMethodID(shortClass, "<init>", "(S)V");
        env->DeleteLocalRef(localShortCls);
        
        //list
        return JNI_VERSION_1_6;
    }

    JNIEXPORT void JNI_OnUnload(JavaVM *vm, void *reserved)
    {
        __android_log_print(ANDROID_LOG_DEBUG, "ncnn", "JNI_OnUnload");

        JNIEnv *env;
        if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK)
        {
            return;
        }

        {
            ncnn::MutexLockGuard g(lock);
            ncnn::MutexLockGuard ocr(lock);
            #if NCNN_VULKAN
                ncnn::destroy_gpu_instance();
            #endif
            delete g_yolo;
            delete g_crnn;

            // 删除全局引用
            if (detectResCls != nullptr)
            {
                env->DeleteGlobalRef(detectResCls);
                detectResCls = nullptr;
            }
            if (rectCls != nullptr)
            {
                env->DeleteGlobalRef(rectCls);
                rectCls = nullptr;
            }
            if (ocrResCls != nullptr)
            {
                env->DeleteGlobalRef(ocrResCls);
                ocrResCls = nullptr;
            }
            if (hashSetClass != nullptr)
            {
                env->DeleteGlobalRef(hashSetClass);
                hashSetClass = nullptr;
            }
            if (shortClass != nullptr)
            {
                env->DeleteGlobalRef(shortClass);
                shortClass = nullptr;
            }
        }
    }

    JNIEXPORT jboolean JNICALL Java_com_smart_autodaily_navpkg_AutoDaily_loadModelSec(
            JNIEnv *env, jobject thiz, jobject assetManager,jstring paramPath, jstring modelPath, jint targetSize, jboolean use_gpu,jshort colorStep, jint lang, jboolean _needOcr, jboolean getColor)
    {
        // Get the actual characters of the string
        const char *param_str = env->GetStringUTFChars(paramPath, nullptr);
        const char *bin_str = env->GetStringUTFChars(modelPath, nullptr);

        paramFile = fdopen(open(param_str, O_RDONLY), "r");
        modelFile = fdopen(open(bin_str, O_RDONLY), "r");

        if (paramFile == nullptr || modelFile == nullptr)
        {
            fclose(paramFile);
            fclose(modelFile);
            env->ReleaseStringUTFChars(paramPath, param_str);
            env->ReleaseStringUTFChars(modelPath, bin_str);
            return JNI_FALSE;
        }
        const int target_sizes[] = {targetSize};
        /*const float mean_vals[][3] ={
                {103.53f, 116.28f, 123.675f},
        };*/
        const float norm_vals[][3] = {
            {1 / 255.f, 1 / 255.f, 1 / 255.f},
        };
        int target_size = target_sizes[0];
        // reload
        {
            ncnn::MutexLockGuard g(lock);
            if (!g_yolo){
                g_yolo = new Yolo;
            }
            #if NCNN_VULKAN
            if (ncnn::get_gpu_count() == 0)
            {
                g_yolo->load(paramFile, modelFile, target_size, norm_vals[0], false);
            }
            else
            {
                g_yolo->load(paramFile, modelFile, target_size, norm_vals[0], use_gpu);
            }
            #else
                g_yolo->load(paramFile, modelFile, target_size, norm_vals[0], false);
            #endif
        }
        fclose(paramFile);
        fclose(modelFile);
        env->ReleaseStringUTFChars(paramPath, param_str);
        env->ReleaseStringUTFChars(modelPath, bin_str);
        if(_needOcr){
            loadOcrModel(env, assetManager, lang, use_gpu, colorStep,getColor);
            needOcr = _needOcr;
        }
        return JNI_TRUE;
    }

    JNIEXPORT jobjectArray JNICALL Java_com_smart_autodaily_navpkg_AutoDaily_detectYolo(JNIEnv *env, jobject thiz, jobject imageData, jint numClasses, jfloat threshold, jfloat nmsThreshold)
    {
        // 检查输入参数
        if (imageData == nullptr || g_yolo == nullptr) {
            __android_log_print(ANDROID_LOG_ERROR, "NCNN", "imageData nullptr");
            return nullptr;
        }
        // 从Bitmap转换为cv::Mat
        cv::Mat image = bitmapToMat(env, imageData);

        if (image.empty()) {
            __android_log_print(ANDROID_LOG_ERROR, "NCNN", "error to bitmapToMat");
            return nullptr;
        }
        cv::Mat dst;
        cv::cvtColor(image, dst, cv::COLOR_RGBA2RGB);
        image.release();
        // 调用detect方法
        std::vector<Object> objects;
        try {
            ncnn::MutexLockGuard g(lock);
            g_yolo->detect(dst, objects, numClasses, threshold, nmsThreshold);
        } catch (const std::exception& e) {
            __android_log_print(ANDROID_LOG_ERROR, "NCNN", "Detection error: %s", e.what());
            dst.release();
            return nullptr;
        }
        
        // 创建并返回结果数组
        jobjectArray result = transformResult(env, objects, dst);
        
        // 释放资源
        dst.release();
        return result;
    }


    JNIEXPORT jint JNICALL Java_com_smart_autodaily_navpkg_AutoDaily_hsvToColor(JNIEnv *env, jobject thiz, jshort h1, jshort s1, jshort v1)
    {
        return  CrnnNet::colorMapping(h1, s1, v1);
    }
}