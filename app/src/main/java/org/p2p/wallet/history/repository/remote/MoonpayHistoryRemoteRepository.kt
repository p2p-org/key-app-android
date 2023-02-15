package org.p2p.wallet.history.repository.remote

import org.p2p.wallet.common.feature_toggles.toggles.remote.SellEnabledFeatureToggle
import org.p2p.wallet.history.model.rpc.HistoryTransaction

import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.moonpay.repository.sell.MoonpaySellRemoteRepository
import org.p2p.wallet.utils.toBase58Instance

class MoonpayHistoryRemoteRepository(
    private val sellEnabledFeatureToggle: SellEnabledFeatureToggle,
    private val repository: MoonpaySellRemoteRepository,
    private val tokenKeyProvider: TokenKeyProvider
) : HistoryRemoteRepository {

    override suspend fun loadHistory(limit: Int, offset: Int): List<HistoryTransaction> {
        if (sellEnabledFeatureToggle.isFeatureEnabled) {
            return repository.getUserSellTransactions(tokenKeyProvider.publicKey.toBase58Instance())
        }
        return emptyList()
    }
}
