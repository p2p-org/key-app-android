package com.wowlet.data.datastore

import com.wowlet.entities.Result
import com.wowlet.entities.responce.HistoricalPrices

interface DetailActivityRepository {
    suspend fun getHistoricalPricesByDate(symbols: String,startTime: Long, endTime: Long): Result<List<HistoricalPrices>>
    suspend fun getAllHistoricalPrices(symbols: String): Result<List<HistoricalPrices>>
}