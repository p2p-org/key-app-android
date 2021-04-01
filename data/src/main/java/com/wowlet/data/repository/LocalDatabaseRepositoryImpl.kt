package com.wowlet.data.repository

import com.wowlet.data.dao.LocalWalletItemDAO
import com.wowlet.data.datastore.LocalDatabaseRepository
import com.wowlet.entities.local.LocalWalletItem

class LocalDatabaseRepositoryImpl(private val dao: LocalWalletItemDAO) : LocalDatabaseRepository {
    override fun saveEditedWallet(localWalletItem: LocalWalletItem) {
        dao.saveWallet(localWalletItem)
    }

    override fun getWallet(walletId: String): LocalWalletItem? = dao.findWalletById(walletId)
}