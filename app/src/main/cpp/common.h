#pragma once
#ifndef COMMON_H_
#define COMMON_H_
#include <opencv2/opencv.hpp>
#include <vector>
#include "clipper.hpp"
#include "net.h"
#include "cpu.h"
//#include <jni.h>
struct TextBox {
    std::vector<cv::Point> boxPoint;
    float score;
};
struct TextLine {
    std::vector<short> label;
    int idx;
    std::string text;
    std::vector<float> charScores;
};

std::vector<cv::Point> getMinBoxes(const std::vector<cv::Point>& inVec, float& minSideLen, float& allEdgeSize);
float boxScoreFast(const cv::Mat& inMat, const std::vector<cv::Point>& inBox);
std::vector<cv::Point> unClip(const std::vector<cv::Point>& inBox, float perimeter, float unClipRatio);
cv::Mat getRotateCropImage(const cv::Mat& src, std::vector<cv::Point> box);
std::vector<cv::Mat> getPartImages(const cv::Mat& src, std::vector<TextBox>& textBoxes);
//cv::Mat matRotateClockWise180(cv::Mat src);
cv::Mat makePadding(cv::Mat& src, int padding);

std::vector<TextBox> findRsBoxes(const cv::Mat& fMapMat, const cv::Mat& norfMapMat,
                                 float boxScoreThresh, float unClipRatio);
#endif //__COMMON_H_
