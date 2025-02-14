//
// Created by PA on 2024/12/13.
//

#ifndef AUTODAILY_OCR_H
#define AUTODAILY_OCR_H
#include "DbNet.h"
#include "CrnnNet.h"
class Ocr
{
public:
        Ocr();
        DbNet* dbNet;
        CrnnNet* crnnNet;
        bool loadModel(AAssetManager* mgr,int lang, bool useGpu, int detectSize,short colorNum,short colorStep);
private:
    const short dstHeight = 48;
    std::vector<std::string> keys;
    const float detMeanValues[3] = { 0.485 * 255, 0.456 * 255, 0.406 * 255 };
    const float detNormValues[3] = { 1.0 / 0.229 / 255.0, 1.0 / 0.224 / 255.0, 1.0 / 0.225 / 255.0 };
    const float rec_mean_vals[3] = { 127.5, 127.5, 127.5 };
    const float rec_norm_vals[3] = { 1.0 / 127.5, 1.0 / 127.5, 1.0 / 127.5 };
};
#endif //AUTODAILY_OCR_H
