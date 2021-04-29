package com.p2p.wallet.qr

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

const val QR_BITMAP_SIZE = 600

class QrCodeInteractor {

    suspend fun generateQrCode(
        address: String,
        qrColors: QrColors
    ): Bitmap = withContext(Dispatchers.Default) {
        BarcodeEncoder.encodeBitmap(
            address,
            BarcodeFormat.QR_CODE,
            QR_BITMAP_SIZE,
            QR_BITMAP_SIZE,
            qrColors
        )
    }
}