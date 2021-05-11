package com.wowlet.data.datastore

import com.wowlet.entities.local.LocalWalletItem

interface LocalDatabaseRepository {
    fun saveEditedWallet(localWalletItem: LocalWalletItem)
    fun getWallet(walletId: String): LocalWalletItem?
}