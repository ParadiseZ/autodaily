
#include "CrnnNet.h"
#include <android/asset_manager_jni.h>
#include <mutex>
#include <unordered_set>

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
    int thread = ncnn::get_cpu_count() - 4;
    if(thread <= 0 ){
        thread =  ncnn::get_cpu_count() - 1;
    }
    ncnn::set_omp_num_threads(thread);

    net.opt = ncnn::Option();
    net.opt.lightmode = true;
    net.opt.openmp_blocktime = 0;
    net.opt.num_threads = 1;
    net.opt.use_vulkan_compute = false;

    net.opt.blob_allocator = &blob_pool_allocator;
    net.opt.workspace_allocator = &workspace_pool_allocator;
    char paramPath[100];
    char mPath[100];
    sprintf(paramPath, "%s.param", modelPath);
    sprintf(mPath, "%s.bin", modelPath);
    net.load_param(mgr,paramPath);
    net.load_model(mgr,mPath);
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

    auto *asset = AAssetManager_open(mgr, filename, AASSET_MODE_BUFFER);
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
    // 处理可能的UTF-8 BOM标记
    char* startPos = buffer;
    if (bufferSize >= 3 && (unsigned char)buffer[0] == 0xEF &&
        (unsigned char)buffer[1] == 0xBB && (unsigned char)buffer[2] == 0xBF) {
        // 跳过UTF-8 BOM标记
        startPos += 3;
    }

    std::string content(startPos);
    std::istringstream inStr(content);
    std::string line;
    int size = 0;
    while (getline(inStr, line)) {
        // 移除可能的回车符
        if (!line.empty() && line.back() == '\r') {
            line.pop_back();
        }
        if (!line.empty()) {
            keys.emplace_back(line);
            size++;
        }
    }
    keySize = static_cast<jsize>(keys.size());
    free(buffer);
    AAsset_close(asset);
    return true;
}

void CrnnNet::getTextLines(std::vector<TextLine>& textLines){
/*#pragma omp parallel
    {*/
    #pragma omp for schedule(dynamic)
    for (auto & textLine : textLines)
    {
        ncnn::Extractor ex = net.create_extractor();
        ncnn::Mat in  = ncnn::Mat::from_pixels(textLine.roi.data, ncnn::Mat::PIXEL_RGB, textLine.roi.cols, textLine.roi.rows);
        in.substract_mean_normalize( mean_vals, norm_vals);
        ex.input("in0", in);
        ncnn::Mat out;
        ex.extract("out0", out);
        auto* floatArray = (float*)out.data;
        std::vector<float> outputData(floatArray, floatArray + out.h * out.w);
        scoreToTextLine(outputData, out.h, out.w,textLine);
    }
}

void CrnnNet::scoreToTextLine(const std::vector<float>& outputData, int h, int w, TextLine & textLine)
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
    if(strRes.empty()){
        return;
    }
    textLine.label.insert( textLine.label.end(), label.begin(), label.end());
    textLine.text = strRes;
    textLine.charScores.insert(textLine.charScores.end(), scores.begin(), scores.end());
}

int CrnnNet::colorMapping(short h, short s, short v) {
    if (v <= 46) {
        return 0;//Black;
    } else if ( s <= 43 && v <= 220 ) {
        return 1;//Gray;
    } else if( s <= 30) {
        return 2; //White
    } else {
        //return h_category[h];、
        int a = 3 + h/10;
        int b = (s - 30)/75;
        int c = (v - 46) / 70;
        b = (a+b)*(a+b+1)/2 + b;
        //return a * 484 + b * 22 + c;
        return (b+c)*(b+c+1)/2 + c;
    }
}