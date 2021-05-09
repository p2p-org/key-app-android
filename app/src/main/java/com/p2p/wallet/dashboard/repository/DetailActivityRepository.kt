package com.p2p.wallet.dashboard.repository

import android.graphics.Bitmap
import com.p2p.wallet.common.network.Result
import com.p2p.wallet.main.api.PriceHistoryResponse

interface DetailActivityRepository {
    suspend fun getHistoricalPricesByDate(
        symbols: String,
        startTime: Long,
        endTime: Long
    ): Result<List<PriceHistoryResponse>>
    fun getQrCode(publicKey: String): Bitmap
    suspend fun getAllHistoricalPrices(symbols: String): Result<List<PriceHistoryResponse>>
}