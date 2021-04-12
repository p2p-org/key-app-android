package com.p2p.wallet.dashboard.repository

import android.graphics.Bitmap
import com.p2p.wallet.common.network.Result
import com.p2p.wallet.dashboard.model.local.ConstWalletItem
import com.p2p.wallet.common.network.HistoricalPrices

interface DashboardRepository {
    fun getQrCode(publicKey: String): Bitmap
    fun getConstWallets(): List<ConstWalletItem>
    suspend fun getHistoricalPrices(symbols: String): Result<List<HistoricalPrices>>
}