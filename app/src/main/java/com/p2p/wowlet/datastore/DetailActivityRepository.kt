package com.p2p.wowlet.datastore

import android.graphics.Bitmap
import com.p2p.wowlet.entities.Result
import com.p2p.wowlet.entities.responce.HistoricalPrices

interface DetailActivityRepository {
    suspend fun getHistoricalPricesByDate(
        symbols: String,
        startTime: Long,
        endTime: Long
    ): Result<List<HistoricalPrices>>
    fun getQrCode(publicKey: String): Bitmap
    suspend fun getAllHistoricalPrices(symbols: String): Result<List<HistoricalPrices>>
}