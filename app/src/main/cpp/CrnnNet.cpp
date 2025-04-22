
#include "CrnnNet.h"
#include <android/asset_manager_jni.h>
#include <unordered_map>
#include <mutex>

CrnnNet::CrnnNet() {
    blob_pool_allocator.set_size_compare_ratio(0.f);
    workspace_pool_allocator.set_size_compare_ratio(0.f);
}

template<class ForwardIterator>
inline static size_t argmax(ForwardIterator first, ForwardIterator last) {
    return std::distance(first, std::max_element(first, last));
}

int CrnnNet::load(AAssetManager* mgr, int _target_size,short _colorStep, const float* _mean_vals, const float* _norm_vals, bool use_gpu, int lang, bool _getColor) {
    const char* modelPath;
    if (lang == 0) {
        modelPath = "ch_rec";
        const char *filename = "ch_keys_v1.txt";
        int redRes = readKeysFromAssets(mgr, filename);
        if (!redRes){
            return -1;
        }
    } else{
        return -1;
    }
    net.clear();
    ncnn::set_cpu_powersave(2);
    ncnn::set_omp_num_threads(ncnn::get_big_cpu_count());

    net.opt = ncnn::Option();
    net.opt.lightmode = true;
#if NCNN_VULKAN
    if (ncnn::get_gpu_count() != 0)
        net.opt.use_vulkan_compute = use_gpu;
#endif
    net.opt.num_threads = ncnn::get_big_cpu_count();
    net.opt.blob_allocator = &blob_pool_allocator;
    net.opt.workspace_allocator = &workspace_pool_allocator;
    char parampath[100];
    char modelpath[100];
    sprintf(parampath, "%s.param", modelPath);
    sprintf(modelpath, "%s.bin", modelPath);
    net.load_param(mgr,parampath);
    net.load_model(mgr,modelpath);
    target_size = _target_size;
    colorStep = _colorStep;
    norm_vals[0] = _norm_vals[0];
    norm_vals[1] = _norm_vals[1];
    norm_vals[2] = _norm_vals[2];
    mean_vals[0] = _mean_vals[0];
    mean_vals[1] = _mean_vals[1];
    mean_vals[2] = _mean_vals[2];
    getColor = _getColor;
    return 0;
}
bool CrnnNet::readKeysFromAssets(AAssetManager *mgr,const char *filename)
{
    if (mgr == nullptr) {
        return false;
    }
    char *buffer;

    auto *asset = AAssetManager_open(mgr, filename, AASSET_MODE_UNKNOWN);
    if (asset == nullptr) {
        return false;
    }
    off_t bufferSize = AAsset_getLength(asset);
    buffer = (char *)malloc(bufferSize + 1);
    if (buffer == nullptr) {
        // 处理内存分配失败的情况
        AAsset_close(asset);
        return false;
    }
    buffer[bufferSize] = 0;
    // 读取资产内容到缓冲区
    if (AAsset_read(asset, buffer, bufferSize) != bufferSize) {
        // 处理读取失败的情况
        free(buffer); // 释放已分配的内存
        AAsset_close(asset);
        // 可能需要返回一个错误码或者抛出异常
        return false; // 或者其他适当的错误处理逻辑
    }
    if(!keys.empty()){
        keys.clear();
    }
    std::istringstream inStr(buffer);
    std::string line;
    int size = 0;
    while (getline(inStr, line)) {
        keys.emplace_back(line);
        size++;
    }
    keySize = static_cast<jsize>(keys.size());
    free(buffer);
    AAsset_close(asset);
    return true;
}

TextLine CrnnNet::getTextLine(cv::Mat& src,cv::Rect rect)
{
    cv::Mat roi = src(rect).clone();
    float scale = (float)target_size / (float)roi.rows;
    auto dstWidth = short((float)roi.cols * scale);
    if (dstWidth > 1056 || dstWidth < 40){//48*22
        return {};
    }
    cv::resize(roi, roi, cv::Size(dstWidth, target_size));
    //if you use PP-OCRv3 you should change PIXEL_RGB to PIXEL_RGB2BGR
    ncnn::Mat input = ncnn::Mat::from_pixels(roi.data, ncnn::Mat::PIXEL_RGB, roi.cols, roi.rows);
    //ncnn::Mat input = ncnn::Mat::from_pixels(srcResize.data, ncnn::Mat::PIXEL_RGB2BGR, srcResize.cols, srcResize.rows);

    input.substract_mean_normalize(mean_vals, norm_vals);

    ncnn::Extractor extractor = net.create_extractor();
    extractor.input("in0", input);

    ncnn::Mat out;
    extractor.extract("out0", out);
    auto* floatArray = (float*)out.data;
    std::vector<float> outputData(floatArray, floatArray + out.h * out.w);
    TextLine res = scoreToTextLine(outputData, out.h, out.w);
    if(res.text.empty()){
        roi.release();
        return {};
    }
    if(getColor){
        res = getColors(res, roi);
    }
    roi.release();
    return res;
}

TextLine CrnnNet::scoreToTextLine(const std::vector<float>& outputData, int h, int w)
{
    std::string strRes;
    std::vector<float> scores;
    std::vector<short> label;
    int lastIndex = 0;
    int maxIndex=0;
    float maxValue;

    for (int i = 0; i < h; i++)
    {
        maxIndex = int(argmax(outputData.begin() + i * w, outputData.begin() + i * w + w));
        maxValue = float(*std::max_element(outputData.begin() + i * w, outputData.begin() + i * w + w));// / partition;
        if (maxIndex > 0 && maxIndex < keySize && (!(i > 0 && maxIndex == lastIndex))) {
            scores.emplace_back(maxValue);
            label.emplace_back(maxIndex-1);
            strRes.append(keys[maxIndex-1]);
        }
        lastIndex = maxIndex;
    }
    return { label,strRes, scores };
}

TextLine  CrnnNet::getColors(TextLine textLine, cv::Mat & src) {
    cv::cvtColor(src, src, cv::COLOR_RGB2HSV);
    // 统计颜色频率，并应用颜色合并
    std::unordered_map<short, int> colorFrequency;
    for (int r = 0; r < 5; ++r) {
        for (int c = 0; c < 5; ++c) {
            cv::Vec3b pixel = src.at<cv::Vec3b>(r, c);
            short color = colorMapping(pixel[0], pixel[1], pixel[2]);
            colorFrequency[color]++;  // 统计颜色频率
            // 关键修改：当 localFreq 大小达到 55时，强制外层循环终止
            if (colorFrequency.size() >= 3) {
                r = 5;  // 将 r 设为 range.end-1，外层循环的 ++r 会使其超过范围
                break;              // 退出内层循环
            }
        }
    }
    for (auto color: colorFrequency) {
        textLine.color.emplace_back(color.first);
    }
    return textLine;
}

static const unsigned char h_category[181] = {
        // 0-10: 3 (Red)11
        3,3,3,3,3,3,3,3,3,3,3,
        // 11-25:4 (Orange)15
        4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,
        // 26-34:5 (Yellow)9
        5,5,5,5,5,5,5,5,5,
        // 35-77:6 (Green)43
        6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,
        // 78-99:7 (Cyan)22
        7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,
        // 100-124:8 (Blue)25
        8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,
        // 125-155:9 (Purple)31
        9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,
        // 156-180:3 (Red)25
        3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3
};
unsigned char CrnnNet::colorMapping(short h, short s, short v) {
    if (v <= 46) {
        return 0;//Black;
    } else if ( s <= 43 && v <= 220 ) {
        return 1;//Gray;
    } else if( s <= 30) {
        return 2; //White
    } else {
        //return h_category[h];
        return (int)h/5;
    }
}

