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
import kotlin.math.roundToInt

const val scaledOriginalWidth = 1280
const val scaledOriginalHeight = 720

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
    picNameList?.forEach {
        println("picPath:$picPath/$it.png")
        val  templateMat= getPicture("$picPath/$it.png")
        var currentSimilar = 1.1
        val scaledOriginalMat = Mat()
        var res = Core.MinMaxLocResult()
        //println("rows: ${originalMat.rows()},cols: ${originalMat.cols()}")
        for (i in 0 until 11){
            Imgproc.resize(originalMat, scaledOriginalMat, Size(originalMat.cols() * (i * 0.02+1), originalMat.rows() * (i * 0.02+1)))
            Imgproc.matchTemplate(scaledOriginalMat, templateMat, result, Imgproc.TM_SQDIFF_NORMED)
            res = Core.minMaxLoc(result)
            if (currentSimilar == 1.1 || res.minVal < currentSimilar  ){
                currentSimilar = res.minVal
                println("i : ${i * 0.02+1}"+"min："+ res.minVal+",max:" +res.maxVal + ", minLoc" + res.minLoc + ", maxLoc" + res.maxLoc)
            }
        }

        // 获取最佳匹配位置


        // 检查相似度阈值
        //println("currentSimilar: $currentSimilar, similarityThreshold: $similarityThreshold")
        if (currentSimilar < similarityThreshold) {
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
}

fun getPicture(picName : String) : Mat{
    val inputStream: InputStream = appCtx.assets.open(picName)
    val bytes = inputStream.readBytes()
    inputStream.close()
    return Imgcodecs.imdecode(MatOfByte(*bytes), Imgcodecs.IMREAD_GRAYSCALE)
}