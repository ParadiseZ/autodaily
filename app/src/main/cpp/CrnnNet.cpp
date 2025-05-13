
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
    ncnn::set_omp_num_threads(ncnn::get_big_cpu_count());

    net.opt = ncnn::Option();

    /*net.opt.lightmode = true;
    net.opt.openmp_blocktime = 0;
    net.opt.num_threads = ncnn::get_big_cpu_count();*/

#if NCNN_VULKAN
    if (ncnn::get_gpu_count() != 0)
        net.opt.use_vulkan_compute = use_gpu;
#endif


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

void CrnnNet::getTextLines(std::vector<TextLine>& textLines){
/*#pragma omp parallel
    {
#pragma omp for*/
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
    //}
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
    if(getColor){
        textLine = getColors(textLine, textLine.roi);
    }
}

TextLine  CrnnNet::getColors(TextLine textLine, cv::Mat & src) {
    std::mutex mutex;
    //std::atomic<bool> should_stop(false);
    int width = src.cols;
    int height = src.rows;
    if (width > height){
        width = height/3;
        height = height/2;
    }else{
        height = width/3;
        width = width/2;
    }
    cv::Mat roi = src(cv::Rect(0,0,width, height));
    //cv::resize(roi,roi, cv::Size(width/2, height/2));
    cv::cvtColor(roi, roi, cv::COLOR_RGB2HSV);

    std::unordered_set<int> allColors;
    cv::parallel_for_(cv::Range(0, roi.rows), [&](const cv::Range& range) {
        std::unordered_set<int> local_colors;
        for (int row = range.start; row < range.end; ++row) {
            const cv::Vec3b* ptr = roi.ptr<cv::Vec3b>(row);
            for (int col = 0; col < roi.cols; ++col) {
                cv::Vec3b pixel = ptr[col];
                int color = colorMapping(pixel[0], pixel[1], pixel[2]);
                local_colors.insert(color);
                /*if (local_colors.size() >= 2) {
                    should_stop = true;
                    break;
                }*/
            }
            //if (should_stop) break;
        }

        // 合并局部统计到全局（需加锁）
        std::lock_guard<std::mutex> lock(mutex);
        allColors.insert(local_colors.begin(), local_colors.end());
    });

    for (auto color: allColors) {
        textLine.color.emplace_back(color);
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

