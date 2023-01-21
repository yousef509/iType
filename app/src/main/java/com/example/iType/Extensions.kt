package com.example.fragmentstutorial

import android.graphics.Bitmap
import android.graphics.Matrix
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

fun applyGrayscaleEffect(source: Bitmap): Bitmap {
    return applyEffect(source)
}
private fun applyEffect(source: Bitmap): Bitmap {
    val inputMat = source.toMat()
    val outputMat = inputMat.applyGrayscaleEffect()
    val outputBitmap = outputMat.toBitmap()

    inputMat.release()
    outputMat.release()

    return outputBitmap
}

fun Bitmap.toMat() : Mat {
    return Mat().also { Utils.bitmapToMat(this, it) }
}

fun Mat.toBitmap() : Bitmap {
    return  Bitmap.createBitmap(
        width(),
        height(),
        Bitmap.Config.ARGB_8888
    ).also {
        Utils.matToBitmap(
            this, it
        )
    }
}
fun Mat.applyGrayscaleEffect(): Mat {
    return createCopy().also { outputMat ->
        Imgproc.cvtColor(this, outputMat, Imgproc.COLOR_BGR2GRAY)
    }
}

fun Mat.createCopy(type: Int = type()): Mat {
    return Mat(rows(), cols(), type)
}

fun rotateBitmap(source: Bitmap, degrees: Float): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(degrees)
    return Bitmap.createBitmap(
        source, 0, 0, source.width, source.height, matrix, true
    )
}