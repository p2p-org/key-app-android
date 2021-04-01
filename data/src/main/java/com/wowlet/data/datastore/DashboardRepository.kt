package com.wowlet.data.datastore

import android.graphics.Bitmap
import com.wowlet.entities.Result
import com.wowlet.entities.local.ConstWalletItem
import com.wowlet.entities.responce.HistoricalPrices

interface DashboardRepository {
    fun getQrCode(publicKey: String): Bitmap
    fun getConstWallets(): List<ConstWalletItem>
    suspend fun getHistoricalPrices(symbols: String): Result<List<HistoricalPrices>>

}