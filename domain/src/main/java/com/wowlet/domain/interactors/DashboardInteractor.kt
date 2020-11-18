package com.wowlet.domain.interactors

import android.graphics.Bitmap
import com.wowlet.entities.local.ConstWalletItem
import com.wowlet.entities.local.SecretKeyCombinationSuccess
import com.wowlet.entities.local.SecretKeyItem
import com.wowlet.entities.local.WalletItem

interface DashboardInteractor {
   fun generateQRrCode(): Bitmap
   suspend fun getWallets():List<WalletItem>
    fun getAddCoinList():List<ConstWalletItem>
}