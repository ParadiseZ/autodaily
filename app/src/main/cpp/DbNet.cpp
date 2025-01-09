#include "DbNet.h"

#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, "DBNET", __VA_ARGS__))

DbNet::DbNet() {
    blob_pool_allocator.set_size_compare_ratio(0.f);
    workspace_pool_allocator.set_size_compare_ratio(0.f);
    target_size = 640;
}

int DbNet::load(AAssetManager* mgr,const char* modelPath, int _target_size, const float* _mean_vals, const float* _norm_vals, bool use_gpu) {
    net.clear();
    ncnn::set_cpu_powersave(2);
    ncnn::set_omp_num_threads(ncnn::get_big_cpu_count());

    net.opt = ncnn::Option();
    net.opt.lightmode = true;
#if NCNN_VULKAN
    LOGD("gpu count %d",ncnn::get_gpu_count());
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

std::vector<TextBox> DbNet::getTextBoxes(const cv::Mat& src, float boxScoreThresh, float boxThresh, float unClipRatio)
{
    int width = src.cols;
    int height = src.rows;
    int w = width;
    int h = height;
    float scale;
    if (w > h)
    {
        scale = (float)target_size / w;
        w = target_size;
        h = h * scale;
    }
    else
    {
        scale = (float)target_size / h;
        h = target_size;
        w = w * scale;
    }

    ncnn::Mat input = ncnn::Mat::from_pixels_resize(src.data, ncnn::Mat::PIXEL_RGB, width, height, w, h);
    //ncnn::Mat input = ncnn::Mat::from_pixels_resize(src.data, ncnn::Mat::PIXEL_BGR2RGB, width, height, w, h);
    // pad to target_size rectangle
    int wpad = (w + 31) / 32 * 32 - w;
    int hpad = (h + 31) / 32 * 32 - h;
    //int wpad = (target_size + 31) / 32 * 32 - w;
    //int hpad = (target_size + 31) / 32 * 32 - h;
    ncnn::Mat in_pad;
    ncnn::copy_make_border(input, in_pad, hpad / 2, hpad - hpad / 2, wpad / 2, wpad - wpad / 2, ncnn::BORDER_CONSTANT, 0.f);
    input.release();
    in_pad.substract_mean_normalize(mean_vals, norm_vals);
    ncnn::Extractor extractor = net.create_extractor();
    extractor.input("in0", in_pad);
    ncnn::Mat out;
    extractor.extract("out0", out);
    cv::Mat fMapMat(in_pad.h, in_pad.w, CV_32FC1, (float*)out.data);
    cv::Mat norfMapMat;
    norfMapMat = fMapMat > boxThresh;

    cv::dilate(norfMapMat, norfMapMat, cv::Mat(), cv::Point(-1, -1), 1);

    std::vector<TextBox> result = findRsBoxes(fMapMat, norfMapMat, boxScoreThresh, unClipRatio);
    for (auto & i : result)
    {
        for (auto & j : i.boxPoint)
        {
            float x = (j.x - (wpad / 2)) / scale;
            float y = (j.y - (hpad / 2)) / scale;
            x = std::max(std::min(x, (float)(width - 1)), 0.f);
            y = std::max(std::min(y, (float)(height - 1)), 0.f);
            j.x = x;
            j.y = y;
        }
    }

    return result;
}