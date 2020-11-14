package com.wowlet.data.datastore

import android.graphics.Bitmap

interface DashboardRepository {
    fun getQrCode(publicKey: String):Bitmap
}