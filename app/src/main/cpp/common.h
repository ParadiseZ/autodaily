#pragma once
#ifndef COMMON_H_
#define COMMON_H_
#include <opencv2/opencv.hpp>
#include <vector>
#include "net.h"
#include "cpu.h"
//#include <jni.h>
struct TextBox {
    std::vector<cv::Point> boxPoint;
    float score;
};
struct TextLine {
    std::vector<short> label;
    std::string text;
    std::vector<float> charScores;
    std::vector<short> color;
};
struct Object
{
    cv::Rect_<float> rect;
    int label;
    float prob;
};

std::vector<cv::Point> getMinBoxes(const std::vector<cv::Point>& inVec, float& minSideLen, float& allEdgeSize);
float boxScoreFast(const cv::Mat& inMat, const std::vector<cv::Point>& inBox);
std::vector<cv::Point> unClip(const std::vector<cv::Point>& inBox, float perimeter, float unClipRatio);
cv::Mat getRotateCropImage(const cv::Mat& src, std::vector<cv::Point> box);
std::vector<cv::Mat> getPartImages(const cv::Mat& src, std::vector<TextBox>& textBoxes);
//cv::Mat matRotateClockWise180(cv::Mat src);
//cv::Mat makePadding(cv::Mat& src, int padding);

std::vector<TextBox> findRsBoxes(const cv::Mat& fMapMat, const cv::Mat& norfMapMat,
                                 float boxScoreThresh, float unClipRatio);

void resize(cv::Mat& input, int targetSize);

cv::Mat bitmapToMat(JNIEnv *env, jobject j_argb8888_bitmap);
static void matToBitmap(JNIEnv *env, cv::Mat &drawMat, jobject obj_bitmap);
#endif //COMMON_H_