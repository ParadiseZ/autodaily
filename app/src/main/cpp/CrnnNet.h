#pragma once
#ifndef CRNNNET_H_
#define CRNNNET_H_

#include "common.h"

class CrnnNet 
{
public:
		CrnnNet();
        ncnn::Net net;
		int load(AAssetManager* mgr, int target_size,short colorStep, const float* _mean_vals, const float* _norm_vals, bool use_gpu = false,int lang=0, bool getColor=false);
        bool readKeysFromAssets(AAssetManager *mgr,const char *filename);
		TextLine getTextLine(cv::Mat& src,cv::Rect rect);
        TextLine scoreToTextLine(const std::vector<float>& outputData, int h, int w);
        static TextLine getColors(TextLine textLine, cv::Mat& src) ;
        static unsigned  char colorMapping(short  h, short s, short v);
private:
		std::vector<std::string> keys;
        int keySize = 0;
        bool getColor = false;
		int target_size = 48;
        short colorStep = 16;
		float mean_vals[3]{};
		float norm_vals[3]{};
		ncnn::UnlockedPoolAllocator blob_pool_allocator;
		ncnn::PoolAllocator workspace_pool_allocator;
};
#endif //CRNNNET_H_