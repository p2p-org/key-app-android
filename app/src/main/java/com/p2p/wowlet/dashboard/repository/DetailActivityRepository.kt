package com.p2p.wowlet.dashboard.repository

import android.graphics.Bitmap
import com.p2p.wowlet.common.network.Result
import com.p2p.wowlet.common.network.HistoricalPrices

interface DetailActivityRepository {
    suspend fun getHistoricalPricesByDate(
        symbols: String,
        startTime: Long,
        endTime: Long
    ): Result<List<HistoricalPrices>>
    fun getQrCode(publicKey: String): Bitmap
    suspend fun getAllHistoricalPrices(symbols: String): Result<List<HistoricalPrices>>
}