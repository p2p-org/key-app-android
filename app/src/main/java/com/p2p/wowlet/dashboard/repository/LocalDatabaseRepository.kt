package com.p2p.wowlet.dashboard.repository

import com.p2p.wowlet.dashboard.model.local.LocalWalletItem

@Deprecated("Each repository should his own dao to interact with DB")
interface LocalDatabaseRepository {
    fun saveEditedWallet(localWalletItem: LocalWalletItem)
    fun getWallet(walletId: String): LocalWalletItem?
}