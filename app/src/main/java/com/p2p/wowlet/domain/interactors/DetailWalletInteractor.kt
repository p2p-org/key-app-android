package com.p2p.wowlet.domain.interactors

import com.github.mikephil.charting.data.Entry
import com.p2p.wowlet.entities.Result
import com.p2p.wowlet.entities.local.ActivityItem
import com.p2p.wowlet.entities.local.EnterWallet
import com.p2p.wowlet.entities.local.WalletItem

interface DetailWalletInteractor {
    suspend fun getActivityList(
        publicKey: String,
        icon: String,
        tokenName: String,
        tokenSymbol: String
    ): Result<List<ActivityItem>>

    suspend fun blockTime(slot: Long): Result<String>
    suspend fun getChatListByDate(
        tokenSymbol: String,
        startTime: Long,
        endTime: Long
    ): Result<List<Entry>>

    suspend fun getChatList(tokenSymbol: String): Result<List<Entry>>
    suspend fun getPercentages(walletItem: WalletItem): Double
    fun generateQRrCode(walletItem: WalletItem): EnterWallet
}
