package com.p2p.wallet.dashboard.repository

import android.graphics.Bitmap
import com.p2p.wallet.common.network.Result
import com.p2p.wallet.dashboard.model.local.ConstWallet
import com.p2p.wallet.main.api.PriceHistoryResponse

interface DashboardRepository {
    fun getQrCode(publicKey: String): Bitmap
    fun getConstWallets(): List<ConstWallet>
}