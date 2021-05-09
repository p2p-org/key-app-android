package com.p2p.wallet.dashboard.repository

import android.graphics.Bitmap
import com.p2p.wallet.dashboard.model.local.ConstWallet

interface DashboardRepository {
    fun getQrCode(publicKey: String): Bitmap
    fun getConstWallets(): List<ConstWallet>
}