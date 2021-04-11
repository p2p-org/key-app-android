package com.p2p.wowlet.repository

import com.p2p.wowlet.dao.WalletDao
import com.p2p.wowlet.datastore.LocalDatabaseRepository
import com.p2p.wowlet.entities.local.LocalWalletItem

class LocalDatabaseRepositoryImpl(private val dao: WalletDao) : LocalDatabaseRepository {
    override fun saveEditedWallet(localWalletItem: LocalWalletItem) {
        dao.saveWallet(localWalletItem)
    }

    override fun getWallet(walletId: String): LocalWalletItem? = dao.findWalletById(walletId)
}