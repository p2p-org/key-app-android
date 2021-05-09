package com.p2p.wallet.dashboard.repository

import android.graphics.Bitmap
import com.p2p.wallet.dashboard.api.RetrofitService
import com.p2p.wallet.dashboard.model.local.ConstWallet
import com.p2p.wallet.utils.WalletDataConst
import net.glxn.qrgen.android.QRCode

class DashboardRepositoryImpl(
    private val api: RetrofitService
) : DashboardRepository {
    override fun getQrCode(publicKey: String): Bitmap {
        return QRCode.from(publicKey).bitmap()
    }

    override fun getConstWallets(): List<ConstWallet> = WalletDataConst.getWalletConstList()
}