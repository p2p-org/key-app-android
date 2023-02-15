package org.p2p.wallet.history.repository.remote

import org.p2p.wallet.history.model.rpc.HistoryTransaction

interface HistoryRemoteRepository {
     suspend fun loadHistory(limit: Int, offset: Int): List<HistoryTransaction>
 }
