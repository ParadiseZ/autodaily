#pragma once
#ifndef CRNNNET_H_
#define CRNNNET_H_

#include "common.h"

class CrnnNet 
{
public:
		CrnnNet();
        ncnn::Net net;
		int load(AAssetManager* mgr,const char* modelPath, short target_size,short color_num,short colorStep, const float* _mean_vals, const float* _norm_vals, bool use_gpu = false);
        void readKeysFromAssets(AAssetManager *mgr,const char *filename);
		TextLine getTextLine(cv::Mat& src, int idx);
		std::vector<TextLine> getTextLines(std::vector<cv::Mat>& partImg);
        TextLine scoreToTextLine(const std::vector<float>& outputData, int h, int w,int idx);
        TextLine getColors(TextLine textLine, cv::Mat& src) const;
        static unsigned  char colorMapping(short  h, short s, short v);
private:
		std::vector<std::string> keys;
		short target_size = 48;
        short color_num = 5;
        short colorStep = 16;
		float mean_vals[3]{};
		float norm_vals[3]{};
		ncnn::UnlockedPoolAllocator blob_pool_allocator;
		ncnn::PoolAllocator workspace_pool_allocator;
};
#endif //CRNNNET_H_