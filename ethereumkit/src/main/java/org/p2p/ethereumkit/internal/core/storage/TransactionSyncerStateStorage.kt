package org.p2p.ethereumkit.internal.core.storage

import org.p2p.ethereumkit.internal.models.TransactionSyncerState

class TransactionSyncerStateStorage(database: TransactionDatabase) {
    private val dao = database.transactionSyncerStateDao()

    fun get(syncerId: String): TransactionSyncerState? =
        dao.get(syncerId)

    fun save(transactionSyncerState: TransactionSyncerState) {
        dao.save(transactionSyncerState)
    }

}
