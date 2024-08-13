package com.smart.autodaily.handler

import com.smart.autodaily.data.entity.Point
import com.smart.autodaily.data.entity.ScriptActionInfo
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.MatOfByte
import org.opencv.core.Size
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import splitties.init.appCtx
import java.io.InputStream

const val scaledOriginalWidth = 1280
const val scaledOriginalHeight = 720
val tempMap = HashMap<String, ArrayList<Mat>>()
/*fun templateMatch(
    picPath : String,
    picNameList : List<String>?,
    originalMat: Mat,
    similarityThreshold: Double,
    is720p: Boolean,
    sai: ScriptActionInfo,
    matchSave: Boolean
) : Boolean{
    val result = Mat()
    picNameList?.forEach {
        println("picPath:$picPath/$it.png")
        val  templateMat= getPicture("$picPath/$it.png")
        if (!is720p) {
            val scaledOriginalMat = Mat()
            Imgproc.resize(originalMat, scaledOriginalMat, Size(scaledOriginalWidth.toDouble(), scaledOriginalHeight.toDouble()))
            // 使用 OpenCV 进行模板匹配

            Imgproc.matchTemplate(scaledOriginalMat, templateMat, result, Imgproc.TM_SQDIFF_NORMED)
        }else{
            Imgproc.matchTemplate(originalMat, templateMat, result, Imgproc.TM_SQDIFF_NORMED)
        }
        // 获取最佳匹配位置
        val res = Core.minMaxLoc(result)
        println("min："+ res.minVal+",max:" +res.maxVal + ", minLoc" + res.minLoc + ", maxLoc" + res.maxLoc)
        // 检查相似度阈值
        if (res.minVal < similarityThreshold) {
            if (matchSave) {
                // 计算模板在原始图像中的位置
                val templateWidth = templateMat.cols()
                val templateHeight = templateMat.rows()
                // 将模板中心点坐标从缩放后的图像映射回原始图像
                if (is720p) {
                    sai.point = Point(
                        (res.minLoc.x + templateWidth / 2).toFloat(),
                        (res.minLoc.y + templateHeight / 2).toFloat()
                    )
                } else {
                    sai.point = Point(
                        ((res.minLoc.x + templateWidth / 2) * originalMat.cols() / scaledOriginalWidth).roundToInt()
                            .toFloat(),
                        ((res.minLoc.y + templateHeight / 2)* originalMat.rows() / scaledOriginalHeight).roundToInt()
                            .toFloat()
                    )
                }
            }else{
                return false
            }
        }else{
            if (matchSave){
                return false
            }
        }
    }
    return true
}*/

fun templateMatch(
    picPath : String,
    picNameList : List<String>?,
    originalMat: Mat,
    similarityThreshold: Double,
    is720p: Boolean,
    sai: ScriptActionInfo,
    matchSave: Boolean
) : Boolean{
    val result = Mat()
    val r = originalMat.rows() / 720
    picNameList?.forEach picName@{  picName ->
        val tempMatList = tempMap.getOrPut(picName) {
            println("getPic:$picPath/$picName.png")
            val tempMatList = ArrayList<Mat>()
            val templateMat = getPicture("$picPath/$picName.png")
            if (r != 1) {
                Imgproc.resize(
                    templateMat,
                    templateMat,
                    Size(
                        templateMat.cols() * r.toDouble(),
                        templateMat.rows() * r.toDouble()
                    )
                )
            }
            for (i in 0 until 5) {
                Imgproc.resize(
                    templateMat,
                    templateMat,
                    Size(templateMat.cols() * (i * 0.02 + 1), templateMat.rows() * (i * 0.02 + 1))
                )
                tempMatList.add(templateMat.clone())
            }
            tempMatList
        }
        println("$picName--------------------")
        tempMatList.forEach {
            Imgproc.matchTemplate(originalMat, it, result, Imgproc.TM_SQDIFF_NORMED)
            val res = Core.minMaxLoc(result)
            println("width : ${it.cols()}"+"min："+ res.minVal+",max:" +res.maxVal + ", minLoc" + res.minLoc + ", maxLoc" + res.maxLoc)
            if (res.minVal <= similarityThreshold) {
                if (matchSave) {
                    // 计算模板在原始图像中的位置
                    val templateWidth = tempMatList[0].cols() * r
                    val templateHeight = tempMatList[0].rows() * r
                    // 将模板中心点坐标从缩放后的图像映射回原始图像
                    sai.point = Point(
                        (res.minLoc.x + templateWidth / 2).toFloat(),
                        (res.minLoc.y + templateHeight / 2).toFloat()
                    )
                }
                return@picName
            }
        }
    }
    //为空，未找到
    if (sai.point == null){
        //若为找到保存，则直接返回false
        if (matchSave){
            return false
        }
        return true
    }else{
        //找到
        //若为找到保存，则直接返回true继续
        if (matchSave){
            return true
        }
        //若为【未找到才满足条件】则返回false不继续
        return false
    }
}

fun getPicture(picName : String) : Mat{
    val inputStream: InputStream = appCtx.assets.open(picName)
    val bytes = inputStream.readBytes()
    inputStream.close()
    return Imgcodecs.imdecode(MatOfByte(*bytes), Imgcodecs.IMREAD_GRAYSCALE)
}