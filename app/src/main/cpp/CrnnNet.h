#pragma once
#ifndef CRNNNET_H_
#define CRNNNET_H_

#include "common.h"
class CrnnNet 
{
public:
		CrnnNet();
        ncnn::Net net;
		int load(AAssetManager* mgr,const char* modelPath, int _target_size, const float* _mean_vals, const float* _norm_vals, bool use_gpu = false);
        void readKeysFromAssets(AAssetManager *mgr,const char *filename);
		TextLine getTextLine(const cv::Mat& src, int idx);
		std::vector<TextLine> getTextLines(std::vector<cv::Mat>& partImg);

		TextLine scoreToTextLine(const std::vector<float>& outputData, int h, int w,int idx);
private:

		std::vector<std::string> keys;
		int target_size = 48;
		float mean_vals[3]{};
		float norm_vals[3]{};
		ncnn::UnlockedPoolAllocator blob_pool_allocator;
		ncnn::PoolAllocator workspace_pool_allocator;
};
#endif //__CRNNNET_H_