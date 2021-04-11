package com.p2p.wowlet.dashboard.repository

import android.graphics.Bitmap
import com.p2p.wowlet.common.network.Result
import com.p2p.wowlet.dashboard.model.local.ConstWalletItem
import com.p2p.wowlet.common.network.HistoricalPrices

interface DashboardRepository {
    fun getQrCode(publicKey: String): Bitmap
    fun getConstWallets(): List<ConstWalletItem>
    suspend fun getHistoricalPrices(symbols: String): Result<List<HistoricalPrices>>
}