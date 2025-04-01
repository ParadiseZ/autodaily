#include "common.h"
//#include <jni.h>
//#include <android/log.h>
bool cvPointCompare(const cv::Point& a, const cv::Point& b) {
    return a.x < b.x;
}
bool compareBoxWidth(const TextBox& a, const TextBox& b)
{
    return abs(a.boxPoint[0].x - a.boxPoint[1].x) > abs(b.boxPoint[0].x - b.boxPoint[1].x);
}

std::vector<cv::Point> getMinBoxes(const std::vector<cv::Point>& inVec, float& minSideLen, float& allEdgeSize) {
    std::vector<cv::Point> minBoxVec;
    cv::RotatedRect textRect = cv::minAreaRect(inVec);
    cv::Mat boxPoints2f;
    cv::boxPoints(textRect, boxPoints2f);

    auto* p1 = (float*)boxPoints2f.data;
    std::vector<cv::Point> tmpVec;
    for (int i = 0; i < 4; ++i, p1 += 2) {
        tmpVec.emplace_back(int(p1[0]), int(p1[1]));
    }

    std::sort(tmpVec.begin(), tmpVec.end(), cvPointCompare);

    minBoxVec.clear();

    int index1, index2, index3, index4;
    if (tmpVec[1].y > tmpVec[0].y) {
        index1 = 0;
        index4 = 1;
    }
    else {
        index1 = 1;
        index4 = 0;
    }

    if (tmpVec[3].y > tmpVec[2].y) {
        index2 = 2;
        index3 = 3;
    }
    else {
        index2 = 3;
        index3 = 2;
    }

    minBoxVec.clear();

    minBoxVec.push_back(tmpVec[index1]);
    minBoxVec.push_back(tmpVec[index2]);
    minBoxVec.push_back(tmpVec[index3]);
    minBoxVec.push_back(tmpVec[index4]);

    minSideLen = (std::min)(textRect.size.width, textRect.size.height);
    allEdgeSize = 2.f * (textRect.size.width + textRect.size.height);

    return minBoxVec;
}

float boxScoreFast(const cv::Mat& inMat, const std::vector<cv::Point>& inBox) {
    std::vector<cv::Point> box = inBox;
    int width = inMat.cols;
    int height = inMat.rows;
    int maxX = -1, minX = 1000000, maxY = -1, minY = 1000000;
    for (auto & i : box) {
        if (maxX < i.x)
            maxX = i.x;
        if (minX > i.x)
            minX = i.x;
        if (maxY < i.y)
            maxY = i.y;
        if (minY > i.y)
            minY = i.y;
    }
    maxX = (std::min)((std::max)(maxX, 0), width - 1);
    minX = (std::max)((std::min)(minX, width - 1), 0);
    maxY = (std::min)((std::max)(maxY, 0), height - 1);
    minY = (std::max)((std::min)(minY, height - 1), 0);

    for (auto & i : box) {
        i.x = i.x - minX;
        i.y = i.y - minY;
    }

    std::vector<std::vector<cv::Point>> maskBox;
    maskBox.push_back(box);
    cv::Mat maskMat(maxY - minY + 1, maxX - minX + 1, CV_8UC1, cv::Scalar(0, 0, 0));
    cv::fillPoly(maskMat, maskBox, cv::Scalar(1, 1, 1), 1);
    return cv::mean(inMat(cv::Rect(cv::Point(minX, minY), cv::Point(maxX + 1, maxY + 1))).clone(),
        maskMat).val[0];
}


cv::Mat getRotateCropImage(const cv::Mat& src, std::vector<cv::Point> box) {
    // 检查输入图像是否为空
    if (src.empty()) {
        return {};
    }
    cv::Mat image;
    src.copyTo(image);
    std::vector<cv::Point> points = box;

    // 检查边界点是否有效
    int collectX[4] = { box[0].x, box[1].x, box[2].x, box[3].x };
    int collectY[4] = { box[0].y, box[1].y, box[2].y, box[3].y };
    int left = int(*std::min_element(collectX, collectX + 4));
    int right = int(*std::max_element(collectX, collectX + 4));
    int top = int(*std::min_element(collectY, collectY + 4));
    int bottom = int(*std::max_element(collectY, collectY + 4));

    // 确保边界在图像范围内
    left = std::max(0, left);
    top = std::max(0, top);
    right = std::min(image.cols, right);
    bottom = std::min(image.rows, bottom);

    // 检查裁剪区域是否有效
    if (right <= left || bottom <= top || left >= image.cols || top >= image.rows) {
        return {};
    }

    cv::Mat imgCrop;
    image(cv::Rect(left, top, right - left, bottom - top)).copyTo(imgCrop);

    // 检查裁剪后图像是否为空
    if (imgCrop.empty()) {
        return {};
    }

    for (auto & point : points) {
        point.x -= left;
        point.y -= top;
    }

    int imgCropWidth = int(sqrt(pow(points[0].x - points[1].x, 2) +
                                pow(points[0].y - points[1].y, 2)));
    int imgCropHeight = int(sqrt(pow(points[0].x - points[3].x, 2) +
                                 pow(points[0].y - points[3].y, 2)));

    // 确保宽高是有效值
    if (imgCropWidth <= 0 || imgCropHeight <= 0) {
        return {};
    }

    cv::Point2f ptsDst[4];
    ptsDst[0] = cv::Point2f(0., 0.);
    ptsDst[1] = cv::Point2f(imgCropWidth, 0.);
    ptsDst[2] = cv::Point2f(imgCropWidth, imgCropHeight);
    ptsDst[3] = cv::Point2f(0.f, imgCropHeight);

    cv::Point2f ptsSrc[4];
    ptsSrc[0] = cv::Point2f(points[0].x, points[0].y);
    ptsSrc[1] = cv::Point2f(points[1].x, points[1].y);
    ptsSrc[2] = cv::Point2f(points[2].x, points[2].y);
    ptsSrc[3] = cv::Point2f(points[3].x, points[3].y);

    cv::Mat M = cv::getPerspectiveTransform(ptsSrc, ptsDst);

    cv::Mat partImg;
    try {
        cv::warpPerspective(imgCrop, partImg, M,
                            cv::Size(imgCropWidth, imgCropHeight),
                            cv::BORDER_REPLICATE);
    } catch (const cv::Exception& e) {
        // 记录错误信息
        __android_log_print(ANDROID_LOG_ERROR, "NCNN", "warpPerspective error: %s", e.what());
        return {};
    }

    // 检查结果图像是否为空
    if (partImg.empty()) {
        return {};
    }

    if (float(partImg.rows) >= float(partImg.cols) * 1.5) {
        cv::Mat srcCopy;
        cv::transpose(partImg, srcCopy);
        cv::flip(srcCopy, srcCopy, 0);
        return srcCopy;
    } else {
        return partImg;
    }
}
std::vector<cv::Mat> getPartImages(const cv::Mat& src, std::vector<TextBox>& textBoxes)
{
    std::sort(textBoxes.begin(), textBoxes.end(), compareBoxWidth);
    std::vector<cv::Mat> partImages;
    if (!textBoxes.empty())
    {
        for (auto & textBox : textBoxes)
        {
            cv::Mat partImg = getRotateCropImage(src, textBox.boxPoint);
            if(!partImg.empty()){
                partImages.emplace_back(partImg);
            }
        }
    }

    return partImages;
}
cv::Mat matRotateClockWise180(cv::Mat src) {
    flip(src, src, 0);
    flip(src, src, 1);
    return src;
}

/*cv::Mat makePadding(cv::Mat& src, int padding) {
    if (padding <= 0) return src;
    cv::Scalar paddingScalar = { 255, 255, 255 };
    cv::Mat paddingSrc;
    cv::copyMakeBorder(src, paddingSrc, padding, padding, padding, padding, cv::BORDER_ISOLATED, paddingScalar);
    return paddingSrc;
}*/



void resize(cv::Mat& input, int targetSize){
    // letterbox pad to multiple of MAX_STRIDE
    int w =  input.cols;
    int h =  input.rows;
    float scale;
    if (w > h)
    {
        scale = (float)targetSize / w;
        w = targetSize;
        h = h * scale;
    }
    else
    {
        scale = (float)targetSize / h;
        h = targetSize;
        w = w * scale;
    }
    cv::resize(input, input, cv::Size(w,h));
}

cv::Mat bitmapToMat(JNIEnv *env, jobject j_argb8888_bitmap)
{
    cv::Mat c_rgba;
    AndroidBitmapInfo j_bitmap_info;
    if (AndroidBitmap_getInfo(env, j_argb8888_bitmap, &j_bitmap_info) < 0)
    {
        return c_rgba;
    }
    if (j_bitmap_info.format != ANDROID_BITMAP_FORMAT_RGBA_8888)
    {
        return c_rgba;
    }
    void *j_bitmap_pixels;
    if (AndroidBitmap_lockPixels(env, j_argb8888_bitmap, &j_bitmap_pixels) < 0)
    {
        return c_rgba;
    }
    cv::Mat j_bitmap_im(static_cast<int>(j_bitmap_info.height),
                        static_cast<int>(j_bitmap_info.width), CV_8UC4,
                        j_bitmap_pixels); // no copied.
    c_rgba = j_bitmap_im;                 // ref only.
    if (AndroidBitmap_unlockPixels(env, j_argb8888_bitmap) < 0)
    {
        return c_rgba;
    }
    return c_rgba;
}

static void matToBitmap(JNIEnv *env, cv::Mat &drawMat, jobject obj_bitmap)
{
    // 锁定画布
    void *pixels;
    AndroidBitmap_lockPixels(env, obj_bitmap, &pixels);
    // 获取Bitmap的信息
    AndroidBitmapInfo bitmapInfo;
    AndroidBitmap_getInfo(env, obj_bitmap, &bitmapInfo);
    // 将Mat数据复制到Bitmap
    cv::Mat bitmapMat(static_cast<int>(bitmapInfo.height), static_cast<int>(bitmapInfo.width), CV_8UC4, pixels);
    drawMat.copyTo(bitmapMat);
    AndroidBitmap_unlockPixels(env, obj_bitmap);
}