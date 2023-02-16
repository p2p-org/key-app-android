package org.p2p.wallet.history.repository.remote

import org.p2p.wallet.common.feature_toggles.toggles.remote.SellEnabledFeatureToggle
import org.p2p.wallet.history.model.HistoryTransaction

import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.moonpay.repository.sell.MoonpaySellRemoteRepository
import org.p2p.wallet.utils.toBase58Instance

class MoonpayHistoryRemoteRepository(
    private val sellEnabledFeatureToggle: SellEnabledFeatureToggle,
    private val repository: MoonpaySellRemoteRepository,
    private val tokenKeyProvider: TokenKeyProvider
) : HistoryRemoteRepository {

    private val allTransactions = mutableListOf<HistoryTransaction>()

    override suspend fun loadHistory(limit: Int): List<HistoryTransaction> {
        if (!sellEnabledFeatureToggle.isFeatureEnabled) return emptyList()

        val newTransactions = repository.getUserSellTransactions(tokenKeyProvider.publicKey.toBase58Instance())
        if (!allTransactions.containsAll(newTransactions)) {
            allTransactions.addAll(newTransactions)
        }
        return allTransactions
    }

    override fun findTransactionById(id: String): HistoryTransaction? {
        return allTransactions.firstOrNull { it.getHistoryTransactionId() == id }
    }
}
