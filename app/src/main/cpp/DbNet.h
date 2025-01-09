#pragma once
#ifndef DBNET_H_
#define DBNET_H_

#include "common.h"
class DbNet
{
public:
	DbNet();
    ncnn::Net net;
    int target_size;
    float mean_vals[3]{};
    float norm_vals[3]{};

    std::vector<TextBox> getTextBoxes(const cv::Mat& src, float boxScoreThresh, float boxThresh, float unClipRatio);

    int load(AAssetManager* mgr,const char* modelPath, int _target_size, const float* _mean_vals, const float* _norm_vals, bool use_gpu);

private:
    ncnn::UnlockedPoolAllocator blob_pool_allocator;
    ncnn::PoolAllocator workspace_pool_allocator;
};
#endif