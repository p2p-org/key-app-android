package org.p2p.wallet.qr.model

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

/**
 * Helper class for encoding barcodes as a Bitmap.
 *
 * Adapted from QRCodeEncoder, from the zxing project:
 * https://github.com/zxing/zxing
 *
 * Licensed under the Apache License, Version 2.0.
 */

object BarcodeEncoder {

    fun encodeBitmap(contents: String, format: BarcodeFormat, width: Int, height: Int, qrParams: QrParams): Bitmap {
        val matrix = MultiFormatWriter()
            .encode(
                contents,
                format,
                width,
                height,
                mapOf(
                    EncodeHintType.CHARACTER_SET to "UTF-8",
                    EncodeHintType.MARGIN to 0,
                    EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.Q
                )
            )
        val pixels = IntArray(matrix.width * matrix.height)
        for (y in 0 until matrix.height) {
            val offset = y * matrix.width
            for (x in 0 until matrix.width) {
                pixels[offset + x] = if (matrix.get(x, y)) qrParams.contentColor else qrParams.backgroundColor
            }
        }
        val bitmap = Bitmap.createBitmap(matrix.width, matrix.height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, matrix.width, 0, 0, matrix.width, matrix.height)
        return bitmap
    }

    fun createQRcode(
        overlay: Bitmap,
        qrCodeData: String,
        width: Int,
        height: Int,
        qrParams: QrParams
    ): Bitmap {
        val hintMap = mapOf(EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H)
        // generating qr code in bitmatrix type
        val charsetData = charset("UTF-8")
        val matrix = MultiFormatWriter().encode(
            String(qrCodeData.toByteArray(charsetData), charsetData),
            BarcodeFormat.QR_CODE, width, height, hintMap
        )

        // converting bitmatrix to bitmap
        val newWidth = matrix.width
        val newHeight = matrix.height
        val pixels = IntArray(newWidth * newHeight)
        // All are 0, or black, by default
        for (y in 0 until newHeight) {
            val offset = y * newWidth
            for (x in 0 until newWidth) {
                pixels[offset + x] = if (matrix.get(x, y)) qrParams.contentColor else qrParams.backgroundColor
            }
        }
        val bitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, newWidth, 0, 0, newWidth, newHeight)
        return mergeBitmaps(overlay, bitmap)
    }

    fun mergeBitmaps(overlay: Bitmap, bitmap: Bitmap): Bitmap {
        val height = bitmap.height
        val width = bitmap.width
        val combined = Bitmap.createBitmap(width, height, bitmap.config)
        val canvas = Canvas(combined)
        val canvasWidth: Int = canvas.width
        val canvasHeight: Int = canvas.height
        canvas.drawBitmap(bitmap, Matrix(), null)
        val centreX = (canvasWidth - overlay.width) / 2
        val centreY = (canvasHeight - overlay.height) / 2
        canvas.drawBitmap(overlay, centreX.toFloat(), centreY.toFloat(), null)
        return combined
    }
}
