package com.wowlet.data.datastore

import android.graphics.Bitmap
import com.wowlet.entities.Result
import com.wowlet.entities.responce.HistoricalPrices

interface DetailActivityRepository {
    suspend fun getHistoricalPricesByDate(symbols: String,startTime: Long, endTime: Long): Result<List<HistoricalPrices>>
    fun getQrCode(publicKey: String): Bitmap
    suspend fun getAllHistoricalPrices(symbols: String): Result<List<HistoricalPrices>>
}