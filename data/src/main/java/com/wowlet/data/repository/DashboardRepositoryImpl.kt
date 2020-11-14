package com.wowlet.data.repository

import android.graphics.Bitmap
import com.wowlet.data.datastore.DashboardRepository
import net.glxn.qrgen.android.QRCode


class DashboardRepositoryImpl : DashboardRepository {
    override fun getQrCode(publicKey: String): Bitmap {
        return QRCode.from(publicKey).bitmap()
    }
}