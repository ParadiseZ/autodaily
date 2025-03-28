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
#include "Ocr.h"

// ARM NEON优化
#if __ARM_NEON
#include <arm_neon.h>
#endif // __ARM_NEON

static Yolo *g_yolo = nullptr;
static Ocr *g_ocr = nullptr;
static ncnn::Mutex lock;
static FILE *paramFile = nullptr;
static FILE *modelFile = nullptr;

#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, "NCNN", __VA_ARGS__))

static cv::Mat bitmapToMat(JNIEnv *env, jobject j_argb8888_bitmap)
{
    cv::Mat c_rgba;
    AndroidBitmapInfo j_bitmap_info;
    if (AndroidBitmap_getInfo(env, j_argb8888_bitmap, &j_bitmap_info) < 0)
    {
        return c_rgba;
    }
    if (j_bitmap_info.format != ANDROID_BITMAP_FORMAT_RGBA_8888)
    {
        return c_rgba;
    }
    void *j_bitmap_pixels;
    if (AndroidBitmap_lockPixels(env, j_argb8888_bitmap, &j_bitmap_pixels) < 0)
    {
        return c_rgba;
    }
    cv::Mat j_bitmap_im(static_cast<int>(j_bitmap_info.height),
                        static_cast<int>(j_bitmap_info.width), CV_8UC4,
                        j_bitmap_pixels); // no copied.
    c_rgba = j_bitmap_im;                 // ref only.
    if (AndroidBitmap_unlockPixels(env, j_argb8888_bitmap) < 0)
    {
        return c_rgba;
    }
    return c_rgba;
}

static void matToBitmap(JNIEnv *env, cv::Mat &drawMat, jobject obj_bitmap)
{
    // 锁定画布
    void *pixels;
    AndroidBitmap_lockPixels(env, obj_bitmap, &pixels);
    // 获取Bitmap的信息
    AndroidBitmapInfo bitmapInfo;
    AndroidBitmap_getInfo(env, obj_bitmap, &bitmapInfo);
    // 将Mat数据复制到Bitmap
    cv::Mat bitmapMat(static_cast<int>(bitmapInfo.height), static_cast<int>(bitmapInfo.width), CV_8UC4, pixels);
    drawMat.copyTo(bitmapMat);
    AndroidBitmap_unlockPixels(env, obj_bitmap);
}

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
// OCR END
static jobjectArray changeDetectResultToJavaArray(JNIEnv *env, const std::vector<Object> &objects)
{
    // 创建Java对象数组
    jobjectArray resultArray = env->NewObjectArray(static_cast<jsize>(objects.size()), detectResCls, nullptr);
    for (size_t i = 0; i < objects.size(); ++i)
    {
        Object obj = objects[i];
        jobject rect = env->NewObject(rectCls,
                                      rectCon,
                                      obj.rect.x, obj.rect.y, obj.rect.width, obj.rect.height);
        jobject detectRes = env->NewObject(detectResCls, detectResCon,
                                           obj.label, obj.prob, rect, (obj.rect.x + obj.rect.width / 2), obj.rect.y + obj.rect.height / 2);
        env->SetObjectArrayElement(resultArray, static_cast<jsize>(i), detectRes);
        env->DeleteLocalRef(rect);
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
        detectResCon = env->GetMethodID(detectResCls, "<init>", "(SFLcom/smart/autodaily/data/entity/Rect;FF)V");
        env->DeleteLocalRef(localDetectResCls);

        jclass localRectCls = env->FindClass("com/smart/autodaily/data/entity/Rect");
        rectCls = (jclass)env->NewGlobalRef(localRectCls);
        rectCon = env->GetMethodID(rectCls, "<init>", "(FFFF)V");
        env->DeleteLocalRef(localRectCls);

        //OCR
        jclass localOcrResCls = env->FindClass("com/smart/autodaily/data/entity/OcrResult");
        ocrResCls = (jclass)env->NewGlobalRef(localOcrResCls);
        ocrResMethod = env->GetMethodID(ocrResCls, "<init>", "(Ljava/util/Set;FFFFFFLjava/util/Set;[S)V");
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
            delete g_ocr;

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

    // public native boolean loadModel(AssetManager mgr, int modelid, int cpugpu);
    /*
    JNIEXPORT jboolean JNICALL Java_com_smart_autodaily_navpkg_AutoDaily_loadModel(JNIEnv *env, jobject thiz, jobject assetManager, jstring modelPath, jint targetSize, jboolean use_gpu)
    {
        // delete g_yolo;
        AAssetManager *mgr = AAssetManager_fromJava(env, assetManager);
        // Get the actual characters of the string
        const char *c_str = env->GetStringUTFChars(modelPath, nullptr);
        // const char* modeltypes[] ={"bh3/cn/model.ncnn"};
        const int target_sizes[] = {targetSize};
        //const float mean_vals[][3] ={ {103.53f, 116.28f, 123.675f},};
        const float norm_vals[][3] = {
            {1 / 255.f, 1 / 255.f, 1 / 255.f},
        };

        // const char* modeltype = modeltypes[0];
        int target_size = target_sizes[0];

        // reload
        {
            ncnn::MutexLockGuard g(lock);
            if (!g_yolo)
                g_yolo = new Yolo;
            if (ncnn::get_gpu_count() == 0)
            {
                g_yolo->load(mgr, c_str, target_size, norm_vals[0], false);
            }
            else
            {
                g_yolo->load(mgr, c_str, target_size, norm_vals[0], use_gpu);
            }
        }
        env->ReleaseStringUTFChars(modelPath, c_str);
        return JNI_TRUE;
    }
    */
    JNIEXPORT jboolean JNICALL Java_com_smart_autodaily_navpkg_AutoDaily_loadModelSec(JNIEnv *env, jobject thiz,
                                                                                      jstring paramPath, jstring modelPath, jint targetSize, jboolean use_gpu)
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
        return JNI_TRUE;
    }

    JNIEXPORT jobjectArray JNICALL Java_com_smart_autodaily_navpkg_AutoDaily_detectYolo(JNIEnv *env, jobject thiz, jobject imageData, jint numClasses, jfloat threshold, jfloat nmsThreshold)
    {
        // 检查输入参数
        if (imageData == nullptr || g_yolo == nullptr) {
            return nullptr;
        }
        // 从Bitmap转换为cv::Mat
        cv::Mat image = bitmapToMat(env, imageData);
        if (image.empty()) {
            return nullptr;
        }

        // 调用detect方法
        std::vector<Object> objects;
        try {
            ncnn::MutexLockGuard g(lock);
            g_yolo->detect(image, objects, numClasses, threshold, nmsThreshold);
        } catch (const std::exception& e) {
            __android_log_print(ANDROID_LOG_ERROR, "NCNN", "Detection error: %s", e.what());
            image.release();
            return nullptr;
        }
        
        // 创建并返回结果数组
        jobjectArray result = changeDetectResultToJavaArray(env, objects);
        
        // 释放资源
        image.release();
        
        return result;
    }
/*
    JNIEXPORT jobjectArray JNICALL Java_com_smart_autodaily_navpkg_AutoDaily_detectAndDraw(JNIEnv *env, jobject thiz, jobject imageData, jint numClasses,
                                                                                           jfloat threshold, jfloat nmsThreshold, jobject drawBitMap)
    {
        cv::Mat img = bitmapToMat(env, imageData);

        std::vector<Object> objects;
        // Yolo detector;
        g_yolo->detect(img, objects, numClasses, threshold, nmsThreshold);
        cv::Mat drawMat = img.clone();
        g_yolo->draw(img, drawMat, objects);
        matToBitmap(env, drawMat, drawBitMap);
        return changeDetectResultToJavaArray(env, objects);
    }
    */

    // OCR
    JNIEXPORT jboolean JNICALL Java_com_smart_autodaily_navpkg_AutoDaily_loadOcr(JNIEnv *env, jobject thiz, jobject assetManager, jint lang, jboolean useGpu, jint detectSize,jshort colorNum,jshort colorStep)
    {
        AAssetManager *mgr = AAssetManager_fromJava(env, assetManager);
        // Get the actual characters of the string

        // reload
        {
            ncnn::MutexLockGuard ocr(lock);
            if (!g_ocr)
                g_ocr = new Ocr;
            g_ocr->loadModel(mgr, lang, useGpu, detectSize,colorNum,colorStep);
        }
        return JNI_TRUE;
    }

    void printLabels(const std::vector<short>& labels,const std::string& txt, const std::vector<short>& color) {
        std::string res;
        res = txt + "[";
        for (size_t i = 0; i < labels.size(); ++i) {
            res += std::to_string(labels[i]); // 格式化 short 类型
            if (i < labels.size() - 1) {
                res += ",";
            }
        }
        res +="], [";
        for (size_t i = 0; i < color.size(); ++i) {
            res += std::to_string(color[i]); // 格式化 short 类型
            if (i < color.size() - 1) {
                res += ",";
            }
        }
        res +="]";
        LOGD("%s", res.c_str());
    }
    JNIEXPORT jobjectArray JNICALL Java_com_smart_autodaily_navpkg_AutoDaily_detectOcr(JNIEnv *env, jobject thiz, jobject bitmap)
    {
        jobjectArray jOcrResArray = nullptr;
        // 检查输入参数
        if (bitmap == nullptr || g_ocr == nullptr) {
            return nullptr;
        }
        
        cv::Mat in = bitmapToMat(env, bitmap);
        if (in.empty()) {
            return nullptr;
        }
        
        cv::Mat rgb;
        cv::cvtColor(in, rgb, cv::COLOR_RGBA2RGB);
        
        // 确保OCR检测器已初始化
        if (!g_ocr->dbNet) {
            in.release();
            rgb.release();
            return nullptr;
        }
        
        std::vector<TextBox> boxResult = g_ocr->dbNet->getTextBoxes(rgb, 0.2, 0.3, 2);
        if (boxResult.empty()) {
            in.release();
            rgb.release();
            return nullptr;
        }
        
        // 获取文本区域图像
        std::vector<cv::Mat> partImages = getPartImages(rgb, boxResult);
        
        // 检查是否有有效的部分图像
        if (partImages.empty() || !g_ocr->crnnNet) {
            in.release();
            rgb.release();
            // 释放partImages中的所有Mat
            for (auto& img : partImages) {
                img.release();
            }
            return nullptr;
        }
        
        // 进行文本识别
        std::vector<TextLine> textLines = g_ocr->crnnNet->getTextLines(partImages);
        if (textLines.empty()) {
            in.release();
            rgb.release();
            // 释放partImages中的所有Mat
            for (auto& img : partImages) {
                img.release();
            }
            return nullptr;
        }
        // 创建结果数组
        jOcrResArray = env->NewObjectArray(static_cast<jsize>(textLines.size()), ocrResCls, nullptr);
        short idx = 0;
        for (const auto &txt : textLines)
        {
            // 检查索引是否有效
            if (txt.idx < 0 || txt.idx >= boxResult.size()) {
                continue;
            }
            //  创建 HashSet 对象
            jobject hashSet = env->NewObject(hashSetClass, hashSetCon);
            for (short k : txt.label)
            {
                jobject shortObj = env->NewObject(shortClass, shortCon, static_cast<jshort>(k));
                env->CallBooleanMethod(hashSet, hashAddMethod, shortObj);
                env->DeleteLocalRef(shortObj);
            }

            // 顺时针
            float x = boxResult[txt.idx].boxPoint[0].x;
            float y = boxResult[txt.idx].boxPoint[0].y;
            float w = boxResult[txt.idx].boxPoint[1].x - x;
            float h = boxResult[txt.idx].boxPoint[3].y - y;
            //printLabels(txt.label, txt.text, txt.color);
            //颜色处理begin
            jobject colorSet = env->NewObject(hashSetClass, hashSetCon);
            auto colorSize = static_cast<jshort>(txt.color.size());
            jshortArray colorArr = env->NewShortArray(colorSize);
            env->SetShortArrayRegion(colorArr, 0, colorSize, txt.color.data());
            //颜色处理end
            
            jobject res = env->NewObject(ocrResCls, ocrResMethod, hashSet, x, y, w, h, x + w/2, y + h/2, colorSet, colorArr);
            env->SetObjectArrayElement(jOcrResArray, idx, res);
            idx++;
            
            // 释放局部引用
            env->DeleteLocalRef(colorSet);
            env->DeleteLocalRef(colorArr);
            env->DeleteLocalRef(res);
            env->DeleteLocalRef(hashSet);
        }
        
        // 释放内存
        in.release();
        rgb.release();
        // 释放partImages中的所有Mat
        for (auto& img : partImages) {
            img.release();
        }
        return jOcrResArray;
    }

    JNIEXPORT jint JNICALL Java_com_smart_autodaily_navpkg_AutoDaily_hsvToColor(JNIEnv *env, jobject thiz, jshort h1, jshort s1, jshort v1)
    {
        return  CrnnNet::colorMapping(h1, s1, v1);
    }

    JNIEXPORT jfloat JNICALL Java_com_smart_autodaily_navpkg_AutoDaily_frameDiff(JNIEnv *env, jobject thiz, jobject beforeBitmap, jobject afterBitmap, jint targetSize, jint range)
    {
        static cv::Mat before;
        static cv::Mat curr;
        static cv::Mat diff;
        before = bitmapToMat(env, beforeBitmap);
        curr =  bitmapToMat(env, afterBitmap);
        resize(before,targetSize);
        resize(curr,targetSize);
        cv::cvtColor(before, before, cv::COLOR_RGBA2GRAY);
        cv::cvtColor(curr, curr, cv::COLOR_RGBA2GRAY);
        cv::absdiff(before, curr, diff);
        return  (float)cv::countNonZero(diff > range) / (float)diff.total();
    }
}

