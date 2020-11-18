package com.wowlet.data.datastore

import android.graphics.Bitmap
import com.wowlet.entities.Result
import com.wowlet.entities.local.ConstWalletItem
import com.wowlet.entities.local.Orderbooks

interface DashboardRepository {
    fun getQrCode(publicKey: String): Bitmap
     fun getConstWallets(): List<ConstWalletItem>

}