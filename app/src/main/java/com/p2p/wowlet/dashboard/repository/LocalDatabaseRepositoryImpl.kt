package com.p2p.wowlet.dashboard.repository

import com.p2p.wowlet.dashboard.db.WalletDao
import com.p2p.wowlet.dashboard.model.local.LocalWalletItem

class LocalDatabaseRepositoryImpl(private val dao: WalletDao) : LocalDatabaseRepository {
    override fun saveEditedWallet(localWalletItem: LocalWalletItem) {
        dao.saveWallet(localWalletItem)
    }

    override fun getWallet(walletId: String): LocalWalletItem? = dao.findWalletById(walletId)
}