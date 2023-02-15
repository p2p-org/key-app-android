package org.p2p.wallet.history.repository.remote

import org.p2p.wallet.history.model.HistoryTransaction

class HistoryRemoteRepositoryFacade(
    private val repositories: List<HistoryRemoteRepository>
) : HistoryRemoteRepository {

    override suspend fun loadHistory(limit: Int, offset: Int): List<HistoryTransaction> {
        return repositories.flatMap { it.loadHistory(limit, offset) }
    }

    override fun findTransactionById(signature: String): HistoryTransaction? {
        repositories.forEach { repository ->
            val foundItem = repository.findTransactionById(signature)
            if (foundItem != null) {
                return foundItem
            }
        }
        return null
    }
}
