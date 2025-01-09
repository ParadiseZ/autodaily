//
// Created by PA on 2024/12/13.
//
#include "Ocr.h"
#include <net.h>
#include <cpu.h>
#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, "ncnn", __VA_ARGS__))

Ocr::Ocr() {
    dbNet = new DbNet;
    crnnNet = new CrnnNet;
}
 bool Ocr::loadModel (AAssetManager* mgr,int lang, bool useGpu) {
    // init param
    {
        int det,  rec;
        if (lang == 0) {
            const char* det_model = "ch_det";
            const char* rec_model = "ch_rec";
            det = dbNet->load(mgr, det_model, target_size,detMeanValues,detNormValues,useGpu);
            rec = crnnNet->load(mgr,rec_model, dstHeight, rec_mean_vals, rec_norm_vals, useGpu);
            /*
            detP = dbNet.load_param(mgr, "ch_det.param");
            detB = dbNet.load_model(mgr, "ch_det.bin");
            recP = crnnNet.load_param(mgr, "ch_rec.param");
            recB = crnnNet.load_model(mgr, "ch_rec.bin");
             */
            const char *filename = "ch_keys_v1.txt";
            crnnNet -> readKeysFromAssets(mgr, filename);
            if (det != 0 ||  rec != 0) {
                return false;
            }
        }
        return true;
    }
}