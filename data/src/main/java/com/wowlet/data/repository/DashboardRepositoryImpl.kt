package com.wowlet.data.repository

import android.graphics.Bitmap
import com.wowlet.data.datastore.DashboardRepository
import com.wowlet.data.util.WalletDataConst
import com.wowlet.entities.Result
import com.wowlet.entities.local.BalanceInfo
import com.wowlet.entities.local.ConstWalletItem
import com.wowlet.entities.local.Orderbooks
import com.wowlet.entities.local.WalletItem
import net.glxn.qrgen.android.QRCode


class DashboardRepositoryImpl : DashboardRepository {
    override fun getQrCode(publicKey: String): Bitmap {
        return QRCode.from(publicKey).bitmap()
    }

    override  fun getConstWallets(): List<ConstWalletItem> = WalletDataConst.walletConstList


}