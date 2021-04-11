package com.p2p.wowlet.datastore

import android.graphics.Bitmap
import com.p2p.wowlet.entities.Result
import com.p2p.wowlet.entities.local.ConstWalletItem
import com.p2p.wowlet.entities.responce.HistoricalPrices

interface DashboardRepository {
    fun getQrCode(publicKey: String): Bitmap
    fun getConstWallets(): List<ConstWalletItem>
    suspend fun getHistoricalPrices(symbols: String): Result<List<HistoricalPrices>>
}