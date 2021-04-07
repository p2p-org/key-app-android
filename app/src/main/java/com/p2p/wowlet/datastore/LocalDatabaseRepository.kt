package com.p2p.wowlet.datastore

import com.p2p.wowlet.entities.local.LocalWalletItem

interface LocalDatabaseRepository {
    fun saveEditedWallet(localWalletItem: LocalWalletItem)
    fun getWallet(walletId: String): LocalWalletItem?
}