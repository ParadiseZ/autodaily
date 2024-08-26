#pragma once
#ifndef YOLO_H
#define YOLO_H

#include <opencv2/core/core.hpp>
#include <net.h>
struct Object
{
    cv::Rect_<float> rect;
    int label;
    float prob;
};

class Yolo
{
public:
    Yolo();
    int load(AAssetManager* mgr,const char* modeltype, int _target_size, const float* _norm_vals, bool use_gpu = false);

    void detect(const cv::Mat& rgb, std::vector<Object>& objects, const int num_classes, float prob_threshold = 0.25f, float nms_threshold = 0.35f);

    int draw(cv::Mat& bgr,cv::Mat& image, const std::vector<Object>& objects);


private:
    ncnn::Net yolo;
    int target_size;
    float norm_vals[3];
    ncnn::UnlockedPoolAllocator blob_pool_allocator;
    ncnn::PoolAllocator workspace_pool_allocator;
};
#endif // NANODET_H