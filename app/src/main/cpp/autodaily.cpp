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

#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, "ncnn", __VA_ARGS__))

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

// OCR BEGIN
// OCR END
extern "C"
{

    JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved)
    {
        __android_log_print(ANDROID_LOG_DEBUG, "ncnn", "JNI_OnLoad");
        ncnn::create_gpu_instance();
        return JNI_VERSION_1_6;
    }

    JNIEXPORT void JNI_OnUnload(JavaVM *vm, void *reserved)
    {
        __android_log_print(ANDROID_LOG_DEBUG, "ncnn", "JNI_OnUnload");

        {
            ncnn::MutexLockGuard g(lock);
            ncnn::MutexLockGuard ocr(lock);
            ncnn::destroy_gpu_instance();
            delete g_yolo;
            delete g_ocr;
        }
    }

    // public native boolean loadModel(AssetManager mgr, int modelid, int cpugpu);
    JNIEXPORT jboolean JNICALL Java_com_smart_autodaily_navpkg_AutoDaily_loadModel(JNIEnv *env, jobject thiz, jobject assetManager, jstring modelPath, jint targetSize, jboolean use_gpu)
    {
        // delete g_yolo;
        AAssetManager *mgr = AAssetManager_fromJava(env, assetManager);
        // Get the actual characters of the string
        const char *c_str = env->GetStringUTFChars(modelPath, nullptr);
        // const char* modeltypes[] ={"bh3/cn/model.ncnn"};
        const int target_sizes[] = {targetSize};
        /*const float mean_vals[][3] ={
                {103.53f, 116.28f, 123.675f},
        };*/
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
            if (!g_yolo)
                g_yolo = new Yolo;
            if (ncnn::get_gpu_count() == 0)
            {
                g_yolo->load(paramFile, modelFile, target_size, norm_vals[0], false);
            }
            else
            {
                g_yolo->load(paramFile, modelFile, target_size, norm_vals[0], use_gpu);
            }
        }
        fclose(paramFile);
        fclose(modelFile);
        env->ReleaseStringUTFChars(paramPath, param_str);
        env->ReleaseStringUTFChars(modelPath, bin_str);
        // 初始化类与构造方法
        detectResCls = env->FindClass("com/smart/autodaily/data/entity/DetectResult");
        detectResCon = env->GetMethodID(detectResCls, "<init>", "(SFLcom/smart/autodaily/data/entity/Rect;FF)V");
        // RectF
        rectCls = env->FindClass("com/smart/autodaily/data/entity/Rect");
        rectCon = env->GetMethodID(rectCls, "<init>", "(FFFF)V");
        return JNI_TRUE;
    }

    JNIEXPORT jobjectArray JNICALL Java_com_smart_autodaily_navpkg_AutoDaily_detectYolo(JNIEnv *env, jobject thiz, jobject imageData, jint numClasses,
                                                                                        jfloat threshold, jfloat nmsThreshold)
    {
        // 1. 从Bitmap转换为cv::Mat
        cv::Mat image = bitmapToMat(env, imageData);

        // 1.1. 转换颜色空间
        // cv::Mat imageBGR;
        // cv::cvtColor(image, imageBGR, cv::COLOR_RGBA2BGR);

        // 2. 调用detect方法
        std::vector<Object> objects;
        // Yolo detector;
        g_yolo->detect(image, objects, numClasses, threshold, nmsThreshold);
        image.release();
        return changeDetectResultToJavaArray(env, objects);
    }

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

    // OCR
    static jclass ocrResCls = nullptr;
    static jmethodID ocrResMethod  = nullptr;
    static jclass hashSetClass = nullptr;
    static jclass shortClass = nullptr;
    static jmethodID hashSetCon =  nullptr;
    static jmethodID shortCon = nullptr;
    static jmethodID addCon = nullptr;
    JNIEXPORT jboolean JNICALL Java_com_smart_autodaily_navpkg_AutoDaily_loadOcr(JNIEnv *env, jobject thiz, jobject assetManager, jint lang, jboolean useGpu)
    {
        AAssetManager *mgr = AAssetManager_fromJava(env, assetManager);
        // Get the actual characters of the string

        // reload
        {
            ncnn::MutexLockGuard ocr(lock);
            if (!g_ocr)
                g_ocr = new Ocr;
            g_ocr->loadModel(mgr, lang, useGpu);
            jclass localCls = env->FindClass("com/smart/autodaily/data/entity/OcrResult");
            ocrResCls = (jclass)env->NewGlobalRef(localCls);
            env->DeleteLocalRef(localCls); // 释放本地引用

            ocrResMethod = env->GetMethodID(ocrResCls, "<init>", "(Ljava/util/Set;FFFFFF)V");
            // class find
            jclass setClass = env->FindClass("java/util/Set");
            hashSetClass = env->FindClass("java/util/HashSet");
            shortClass = env->FindClass("java/lang/Short");
            // method find, hash set
            hashSetCon = env->GetMethodID(hashSetClass, "<init>", "()V");
            // 获取 Short 的构造函数
            shortCon = env->GetMethodID(shortClass, "<init>", "(S)V");
            // 获取 Set.add 方法
            addCon = env->GetMethodID(setClass, "add", "(Ljava/lang/Object;)Z");

            env->DeleteLocalRef(setClass); // 释放本地引用
        }
        return JNI_TRUE;
    }
    JNIEXPORT jobjectArray JNICALL Java_com_smart_autodaily_navpkg_AutoDaily_detectOcr(JNIEnv *env, jobject thiz, jobject bitmap)
    {
        cv::Mat in = bitmapToMat(env, bitmap);
        cv::Mat rgb;
        cv::cvtColor(in, rgb, cv::COLOR_BGR2RGB);
        in.release();
        std::vector<TextBox> boxResult = g_ocr->dbNet->getTextBoxes(rgb, 0.2, 0.3, 2);
        LOGD("start getPartImages");
        std::vector<cv::Mat> partImages = getPartImages(rgb, boxResult);
        LOGD("start getTextLines");
        std::vector<TextLine> textLines = g_ocr->crnnNet->getTextLines(partImages);
        rgb.release();
        LOGD("getTextEnd");
        // objects to Obj[]
        jobjectArray jOcrResArray = env->NewObjectArray(static_cast<jsize>(textLines.size()), ocrResCls, nullptr);
        LOGD("jOcrResArray end");
        short idx = 0;
        for (const auto & txt : textLines)
        {
            // const char * txt =  textLines[i].text.c_str();
            //  创建 HashSet 对象
            jobject hashSet = env->NewObject(hashSetClass, hashSetCon);
            for (short k : txt.label)
            {
                jobject shortObj = env->NewObject(shortClass, shortCon, static_cast<jshort>(k));
                // add
                env->CallBooleanMethod(hashSet, addCon, shortObj);
                // 释放局部引用
                env->DeleteLocalRef(shortObj);
            }
            LOGD("hashset end");
            // env->SetShortArrayRegion(jLabelArray,0, len,buffer);
            float x = boxResult[txt.idx].boxPoint[0].x;
            float y = boxResult[txt.idx].boxPoint[0].y;
            float w = boxResult[txt.idx].boxPoint[1].x;
            float h = boxResult[txt.idx].boxPoint[1].y;
            float cx = boxResult[txt.idx].boxPoint[2].x;
            float cy = boxResult[txt.idx].boxPoint[2].y;
            float x3 = boxResult[txt.idx].boxPoint[3].x;
            float y3 = boxResult[txt.idx].boxPoint[3].y;
            LOGD("x %f,y %f,w %f,h %f,cx %f,cy %f,x3 %f,y3 %f, str %s\n", x, y, w, h, cx, cy, x3, y3, txt.text.c_str());
            jobject res = env->NewObject(ocrResCls, ocrResMethod, hashSet, x, y, w, h, cx, cy);

            env->SetObjectArrayElement(jOcrResArray, idx, res);
            idx++;
            // 释放局部引用
            env->DeleteLocalRef(res);
            env->DeleteLocalRef(hashSet);
        }
        return jOcrResArray;
    }
}
