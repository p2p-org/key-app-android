package org.p2p.ethereumkit.api.storage

import org.p2p.ethereumkit.api.models.AccountState
import org.p2p.ethereumkit.api.models.LastBlockHeight
import org.p2p.ethereumkit.core.IApiStorage

class ApiStorage(
        private val database: ApiDatabase
) : IApiStorage {

    override fun getLastBlockHeight(): Long? {
        return database.lastBlockHeightDao().getLastBlockHeight()?.height
    }

    override fun saveLastBlockHeight(lastBlockHeight: Long) {
        database.lastBlockHeightDao().insert(LastBlockHeight(lastBlockHeight))
    }

    override fun saveAccountState(state: AccountState) {
        database.balanceDao().insert(state)
    }

    override fun getAccountState(): AccountState? {
        return database.balanceDao().getAccountState()
    }

}
