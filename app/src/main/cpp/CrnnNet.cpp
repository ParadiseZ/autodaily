
#include "CrnnNet.h"
#include <android/asset_manager_jni.h>

CrnnNet::CrnnNet() {
    blob_pool_allocator.set_size_compare_ratio(0.f);
    workspace_pool_allocator.set_size_compare_ratio(0.f);
    target_size = 640;
}

template<class ForwardIterator>
inline static size_t argmax(ForwardIterator first, ForwardIterator last) {
    return std::distance(first, std::max_element(first, last));
}

int CrnnNet::load(AAssetManager* mgr,const char* modelPath, int _target_size, const float* _mean_vals, const float* _norm_vals, bool use_gpu) {
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
    norm_vals[0] = _norm_vals[0];
    norm_vals[1] = _norm_vals[1];
    norm_vals[2] = _norm_vals[2];
    mean_vals[0] = _mean_vals[0];
    mean_vals[1] = _mean_vals[1];
    mean_vals[2] = _mean_vals[2];
    return 0;
}
void CrnnNet::readKeysFromAssets(AAssetManager *mgr,const char *filename)
{
    if (mgr == nullptr) {
        return;
    }
    char *buffer;

    auto *asset = AAssetManager_open(mgr, filename, AASSET_MODE_UNKNOWN);
    if (asset == nullptr) {
        return;
    }
    off_t bufferSize = AAsset_getLength(asset);
    buffer = (char *)malloc(bufferSize + 1);
    if (buffer == nullptr) {
        // 处理内存分配失败的情况
        AAsset_close(asset);
        return;
    }
    buffer[bufferSize] = 0;
    // 读取资产内容到缓冲区
    if (AAsset_read(asset, buffer, bufferSize) != bufferSize) {
        // 处理读取失败的情况
        free(buffer); // 释放已分配的内存
        AAsset_close(asset);
        // 可能需要返回一个错误码或者抛出异常
        return; // 或者其他适当的错误处理逻辑
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
    free(buffer);
    AAsset_close(asset);
}

TextLine CrnnNet::getTextLine(const cv::Mat& src, int idx)
{
    float scale = (float)target_size / (float)src.rows;
    int dstWidth = int((float)src.cols * scale);
    cv::Mat srcResize;
    cv::resize(src, srcResize, cv::Size(dstWidth, target_size));
    //if you use PP-OCRv3 you should change PIXEL_RGB to PIXEL_RGB2BGR
    ncnn::Mat input = ncnn::Mat::from_pixels(srcResize.data, ncnn::Mat::PIXEL_RGB, srcResize.cols, srcResize.rows);
    //ncnn::Mat input = ncnn::Mat::from_pixels(srcResize.data, ncnn::Mat::PIXEL_RGB2BGR, srcResize.cols, srcResize.rows);
    
    input.substract_mean_normalize(mean_vals, norm_vals);

    ncnn::Extractor extractor = net.create_extractor();
    //extractor.set_num_threads(2);
    //cv_show("resize",srcResize,0);
    extractor.input("in0", input);

    ncnn::Mat out;
    extractor.extract("out0", out);
    auto* floatArray = (float*)out.data;
    std::vector<float> outputData(floatArray, floatArray + out.h * out.w);
    TextLine res = scoreToTextLine(outputData, out.h, out.w,idx);
    return res;
}

std::vector<TextLine> CrnnNet::getTextLines(std::vector<cv::Mat>& partImg) {
    int size = partImg.size();
    std::vector<TextLine> textLines;
    for (int i = 0; i < size; ++i)
    {
        TextLine textLine = getTextLine(partImg[i], i);
        if (!textLine.text.empty()){
            textLines.emplace_back(textLine);
        }

    }
    return textLines;
}

TextLine CrnnNet::scoreToTextLine(const std::vector<float>& outputData, int h, int w,int idx)
{
    int keySize = keys.size();
    std::string strRes;
    std::vector<float> scores;
    std::vector<short> label;
    int lastIndex = 0;
    int maxIndex;
    float maxValue;

    for (int i = 0; i < h; i++)
    {
        maxIndex = 0;
        maxValue = -1000.f;
        maxIndex = int(argmax(outputData.begin() + i * w, outputData.begin() + i * w + w));
        maxValue = float(*std::max_element(outputData.begin() + i * w, outputData.begin() + i * w + w));// / partition;
        if (maxIndex > 0 && maxIndex < keySize && (!(i > 0 && maxIndex == lastIndex))) {
            scores.emplace_back(maxValue);
            label.emplace_back(maxIndex-1);
            strRes.append(keys[maxIndex-1]);
            if(label.size()>12){
                return {};
            }
        }
        lastIndex = maxIndex;
    }
    if (label.size()<2){
        return {};
    }
    return { label, idx,strRes, scores };
}