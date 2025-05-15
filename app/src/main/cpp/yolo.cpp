#include "yolo.h"

#include <memory>
#include <vector>
#include <algorithm>
#include "cpu.h"
#include "layer.h"

#include <opencv2/imgproc/imgproc.hpp>

//参考来自https://github.com/Tencent/ncnn
constexpr auto MAX_STRIDE = 32;

static inline float intersection_area(const Object& a, const Object& b)
{
    cv::Rect_<float> inter = a.rect & b.rect;
    return inter.area();
}

static void qsort_descent_inplace(std::vector<Object>& objects, int left, int right)
{
    int i = left;
    int j = right;
    float p = objects[(left + right) / 2].prob;

    while (i <= j)
    {
        while (objects[i].prob > p)
            i++;

        while (objects[j].prob < p)
            j--;

        if (i <= j)
        {
            // swap
            std::swap(objects[i], objects[j]);

            i++;
            j--;
        }
    }

#pragma omp parallel sections
    {
#pragma omp section
        {
            if (left < j) qsort_descent_inplace(objects, left, j);
        }
#pragma omp section
        {
            if (i < right) qsort_descent_inplace(objects, i, right);
        }
    }
}

static void qsort_descent_inplace(std::vector<Object>& objects)
{
    if (objects.empty())
        return;

    qsort_descent_inplace(objects, 0, objects.size() - 1);
}

static void nms_sorted_bboxes(const std::vector<Object> &faceobjects, std::vector<int> &picked,float nms_threshold)
{
    picked.clear();

    const int n = faceobjects.size();

    std::vector<float> areas(n);
    for (int i = 0; i < n; i++)
    {
        areas[i] = faceobjects[i].rect.area();
    }

    for (int i = 0; i < n; i++)
    {
        const Object& a = faceobjects[i];

        int keep = 1;
        for (int j : picked)
        {
            const Object& b = faceobjects[j];

            if (a.label != b.label)
                continue;

            // intersection over union
            float inter_area = intersection_area(a, b);
            float union_area = areas[i] + areas[j] - inter_area;
            // float IoU = inter_area / union_area
            if (inter_area / union_area > nms_threshold)
                keep = 0;
        }

        if (keep)
            picked.emplace_back(i);
    }
}

static inline float sigmoid(float x)
{
    return static_cast<float>(1.f / (1.f + exp(-x)));
}

static inline float clampf(float d, float min, float max)
{
    const float t = d < min ? min : d;
    return t > max ? max : t;
}

static void parse_yolov8_detections(
        float* inputs, float confidence_threshold,
        int num_channels, int num_anchors, int num_labels,
        int infer_img_width, int infer_img_height,
        std::vector<Object>& objects)
{
    std::vector<Object> detections;
    cv::Mat output = cv::Mat((int)num_channels, (int)num_anchors, CV_32F, inputs).t();
    for (int i = 0; i < num_anchors; i++)
    {
        const float* row_ptr = output.row(i).ptr<float>();
        const float* bboxes_ptr = row_ptr;
        const float* scores_ptr = row_ptr + 4;
        const float* max_s_ptr = std::max_element(scores_ptr, scores_ptr + num_labels);
        float score = *max_s_ptr;
        if (score > confidence_threshold)
        {
            float x = *bboxes_ptr++;
            float y = *bboxes_ptr++;
            float w = *bboxes_ptr++;
            float h = *bboxes_ptr;

            float x0 = clampf((x - 0.5f * w), 0.f, (float)infer_img_width);
            float y0 = clampf((y - 0.5f * h), 0.f, (float)infer_img_height);
            float x1 = clampf((x + 0.5f * w), 0.f, (float)infer_img_width);
            float y1 = clampf((y + 0.5f * h), 0.f, (float)infer_img_height);

            cv::Rect_<float> bbox;
            bbox.x = x0;
            bbox.y = y0;
            bbox.width = x1 - x0;
            bbox.height = y1 - y0;
            Object object;
            object.label = max_s_ptr - scores_ptr;
            object.prob = score;
            object.rect = bbox;
            detections.emplace_back(object);
        }
    }
    objects = detections;
}

Yolo::Yolo()
{
    blob_pool_allocator.set_size_compare_ratio(0.f);
    workspace_pool_allocator.set_size_compare_ratio(0.f);
    target_size = 640;
}
int Yolo::load(AAssetManager* mgr,const char* modeltype, int _target_size, const float* _norm_vals, bool use_gpu)
{
    /*yolo.clear();
    ncnn::set_cpu_powersave(0);
    ncnn::set_omp_num_threads(ncnn::get_little_cpu_count());
    yolo.opt = ncnn::Option();
#if NCNN_VULKAN
    if (ncnn::get_gpu_count() != 0)
        yolo.opt.use_vulkan_compute = use_gpu;
#endif
    yolo.opt.num_threads = ncnn::get_big_cpu_count();
    yolo.opt.blob_allocator = &blob_pool_allocator;
    yolo.opt.workspace_allocator = &workspace_pool_allocator;
    char parampath[100];
    char modelpath[100];
    sprintf(parampath, "%s.param", modeltype);
    sprintf(modelpath, "%s.bin", modeltype);
    yolo.load_param(mgr,parampath);
    yolo.load_model(mgr,modelpath);
    target_size = _target_size;
    norm_vals[0] = _norm_vals[0];
    norm_vals[1] = _norm_vals[1];
    norm_vals[2] = _norm_vals[2];
    return 0;*/
}
int Yolo::load(FILE * paramFile,FILE * modelFile, int _target_size, const float* _norm_vals, bool use_gpu)
{
    //0:全部 1:小核 2:大核
    yolo.clear();
    ncnn::set_cpu_powersave(1);
    ncnn::set_omp_num_threads(ncnn::get_little_cpu_count());

    yolo.opt = ncnn::Option();

    /*yolo.opt.openmp_blocktime = 0;
    yolo.opt.lightmode = true;
    yolo.opt.num_threads = ncnn::get_little_cpu_count();*/
#if NCNN_VULKAN
    if (ncnn::get_gpu_count() != 0)
        yolo.opt.use_vulkan_compute = use_gpu;
#endif

    yolo.opt.blob_allocator = &blob_pool_allocator;
    yolo.opt.workspace_allocator = &workspace_pool_allocator;
    yolo.load_param(paramFile);
    yolo.load_model(modelFile);
    target_size = _target_size;
    norm_vals[0] = _norm_vals[0];
    norm_vals[1] = _norm_vals[1];
    norm_vals[2] = _norm_vals[2];
    return 0;
}
void Yolo::detect(const cv::Mat& bgr, std::vector<Object>& objects,std::vector<TextLine>& txts,CrnnNet *& g_crnn, const int num_labels, float prob_threshold, float nms_threshold)
{
    int img_w = bgr.cols;
    int img_h = bgr.rows;

    // letterbox pad to multiple of MAX_STRIDE
    int w = img_w;
    int h = img_h;
    float scale;
    if (w > h)
    {
        scale = (float)target_size / w;
        w = target_size;
        h = h * scale;
    }
    else
    {
        scale = (float)target_size / h;
        h = target_size;
        w = w * scale;
    }
    ncnn::Mat in = ncnn::Mat::from_pixels_resize(bgr.data, ncnn::Mat::PIXEL_RGB, img_w, img_h, w, h);

    int wpad = (target_size + MAX_STRIDE - 1) / MAX_STRIDE * MAX_STRIDE - w;
    int hpad = (target_size + MAX_STRIDE - 1) / MAX_STRIDE * MAX_STRIDE - h;
    ncnn::Mat in_pad;
    ncnn::copy_make_border(in, in_pad, hpad / 2, hpad - hpad / 2, wpad / 2, wpad - wpad / 2, ncnn::BORDER_CONSTANT, 114.f);
    //const float norm_vals[3] = { 1 / 255.f, 1 / 255.f, 1 / 255.f };
    in_pad.substract_mean_normalize(0, norm_vals);
    ncnn::Extractor ex = yolo.create_extractor();
    ex.input("in0", in_pad);

    std::vector<Object> proposals;

    // stride 32
    {
        ncnn::Mat out;
        ex.extract("out0", out);
        std::vector<Object> objects32;
        //const int num_labels = 80; // COCO has detect 80 object labels.
        parse_yolov8_detections(
                (float*)out.data, prob_threshold,
                out.h, out.w, num_labels,
                in_pad.w, in_pad.h,
                objects32);
        proposals.insert(proposals.end(), objects32.begin(), objects32.end());
    }
    // sort all proposals by score from highest to lowest
    qsort_descent_inplace(proposals);
    // apply nms with nms_threshold
    std::vector<int> picked;
    nms_sorted_bboxes(proposals, picked, nms_threshold);

    int count = picked.size();
    //objects.resize(count);
    for (int i = 0; i < count; i++)
    {
        Object obj = proposals[picked[i]];

        // adjust offset to original unpadded
        float x0 = (obj.rect.x - (wpad / 2)) / scale;
        float y0 = (obj.rect.y - (hpad / 2)) / scale;
        float x1 = (obj.rect.x + obj.rect.width - (wpad / 2)) / scale;
        float y1 = (obj.rect.y + obj.rect.height - (hpad / 2)) / scale;

        // clip
        x0 = std::max(std::min(x0, (float)(img_w - 1)), 0.f);
        y0 = std::max(std::min(y0, (float)(img_h - 1)), 0.f);
        x1 = std::max(std::min(x1, (float)(img_w - 1)), 0.f);
        y1 = std::max(std::min(y1, (float)(img_h - 1)), 0.f);

        obj.rect.x = x0;
        obj.rect.y = y0;
        obj.rect.width = x1 - x0;
        obj.rect.height = y1 - y0;

        if(obj.label == 0){
            float scaleTxt =  48.0f / obj.rect.height;
            auto dstWidth = int(obj.rect.width * scaleTxt);
            if (dstWidth > 1056 || dstWidth < 40){//48*22
                continue;
            }
            cv::Mat roi = bgr(obj.rect).clone();
            cv::resize(roi,roi, cv::Size(dstWidth, g_crnn->target_size));
            txts.emplace_back(TextLine{roi, {} ,{},{}, {}, obj});
        } else{
            objects.push_back(obj);
        }
    }
}

int Yolo::draw(cv::Mat& bgr, cv::Mat& image,const std::vector<Object>& objects)
{
    static const unsigned char colors[19][3] = {
            {54, 67, 244},
            {99, 30, 233},
            {176, 39, 156},
            {183, 58, 103},
            {181, 81, 63},
            {243, 150, 33},
            {244, 169, 3},
            {212, 188, 0},
            {136, 150, 0},
            {80, 175, 76},
            {74, 195, 139},
            {57, 220, 205},
            {59, 235, 255},
            {7, 193, 255},
            {0, 152, 255},
            {34, 87, 255},
            {72, 85, 121},
            {158, 158, 158},
            {139, 125, 96}
    };
    int color_index = 0;

    image = bgr.clone();

    for (const auto & obj : objects)
    {
        const unsigned char* color = colors[color_index % 19];
        color_index++;

        cv::Scalar cc(color[0], color[1], color[2]);

        fprintf(stderr, "%d = %.5f at %.2f %.2f %.2f x %.2f\n", obj.label, obj.prob,
                obj.rect.x, obj.rect.y, obj.rect.width, obj.rect.height);

        cv::rectangle(image, obj.rect, cc, 2);

        char text[256];
        //sprintf(text, "%s %.1f%%", obj.label, obj.prob * 100);
        sprintf(text, "idx:%d %.1f%%", obj.label, obj.prob * 100);

        int baseLine = 0;
        cv::Size label_size = cv::getTextSize(text, cv::FONT_HERSHEY_SIMPLEX, 0.5, 1, &baseLine);

        int x = obj.rect.x;
        int y = obj.rect.y - label_size.height - baseLine;
        if (y < 0)
            y = 0;
        if (x + label_size.width > image.cols)
            x = image.cols - label_size.width;

        cv::rectangle(image, cv::Rect(cv::Point(x, y), cv::Size(label_size.width, label_size.height + baseLine)),
                      cc, -1);

        cv::putText(image, text, cv::Point(x, y + label_size.height),
                    cv::FONT_HERSHEY_SIMPLEX, 0.5, cv::Scalar(255, 255, 255));
    }
    //cv_show("result", image);
    return 0;
}
