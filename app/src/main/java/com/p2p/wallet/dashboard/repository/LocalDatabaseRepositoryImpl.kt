package com.p2p.wallet.dashboard.repository

import com.p2p.wallet.dashboard.db.WalletDao
import com.p2p.wallet.dashboard.model.local.LocalWalletItem

class LocalDatabaseRepositoryImpl(private val dao: WalletDao) : LocalDatabaseRepository {
    override fun saveEditedWallet(localWalletItem: LocalWalletItem) {
        dao.saveWallet(localWalletItem)
    }

    override fun getWallet(walletId: String): LocalWalletItem? = dao.findWalletById(walletId)
}