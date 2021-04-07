package com.p2p.wowlet.domain.interactors

import com.p2p.wowlet.entities.local.SendTransactionModel
import com.p2p.wowlet.entities.Result
import com.p2p.wowlet.entities.local.ActivityItem
import com.p2p.wowlet.entities.local.WalletItem
import java.math.BigDecimal

interface SendCoinInteractor {
    suspend fun sendCoin(coinData: SendTransactionModel): Result<ActivityItem>

    suspend fun getFee(): Result<BigDecimal>

    fun saveWalletItem(walletItem: WalletItem?)

    suspend fun getWalletItem(): Result<WalletItem>?
}