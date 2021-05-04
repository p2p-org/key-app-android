package com.p2p.wallet.qr.model

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter

/**
 * Helper class for encoding barcodes as a Bitmap.
 *
 * Adapted from QRCodeEncoder, from the zxing project:
 * https://github.com/zxing/zxing
 *
 * Licensed under the Apache License, Version 2.0.
 */

object BarcodeEncoder {

    fun encodeBitmap(contents: String, format: BarcodeFormat, width: Int, height: Int, qrColors: QrColors): Bitmap {
        val matrix = MultiFormatWriter()
            .encode(
                contents,
                format,
                width,
                height,
                mapOf(EncodeHintType.CHARACTER_SET to "UTF-8")
            )
        val pixels = IntArray(matrix.width * matrix.height)
        for (y in 0 until matrix.height) {
            val offset = y * matrix.width
            for (x in 0 until matrix.width) {
                pixels[offset + x] = if (matrix.get(x, y)) qrColors.contentColor else qrColors.backgroundColor
            }
        }
        val bitmap = Bitmap.createBitmap(matrix.width, matrix.height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, matrix.width, 0, 0, matrix.width, matrix.height)
        return bitmap
    }
}