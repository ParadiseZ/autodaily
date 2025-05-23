#pragma once
#ifndef YOLO_H
#define YOLO_H

#include <opencv2/core.hpp>
#include <net.h>
#include "CrnnNet.h"

class Yolo
{
public:
    Yolo();
    int load(FILE * paramFile,FILE * modelFile, int _target_size, const float* _norm_vals, bool use_gpu = false);

    void detect(const cv::Mat& rgb, std::vector<Object>& objects,std::vector<TextLine>& txt,CrnnNet *& g_crnn, int num_classes, float prob_threshold = 0.25f, float nms_threshold = 0.45f);

private:
    ncnn::Net yolo;
    int target_size;
    float norm_vals[3]{};
    ncnn::UnlockedPoolAllocator blob_pool_allocator;
    ncnn::PoolAllocator workspace_pool_allocator;
};
#endif // YOLO_H