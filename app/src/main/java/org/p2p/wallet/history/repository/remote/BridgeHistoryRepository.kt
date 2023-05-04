package org.p2p.wallet.history.repository.remote

import org.p2p.wallet.bridge.api.mapper.BridgeMapper
import org.p2p.wallet.bridge.claim.repository.EthereumBridgeLocalRepository
import org.p2p.wallet.common.feature_toggles.toggles.remote.EthAddressEnabledFeatureToggle
import org.p2p.wallet.history.model.HistoryPagingResult
import org.p2p.wallet.history.model.HistoryPagingState
import org.p2p.wallet.history.model.HistoryTransaction

class BridgeHistoryRepository(
    private val claimLocalRepository: EthereumBridgeLocalRepository,
    private val bridgeMapper: BridgeMapper,
    private val ethereumFeatureToggle: EthAddressEnabledFeatureToggle,
) : HistoryRemoteRepository {

    override suspend fun loadHistory(limit: Int, mintAddress: String): HistoryPagingResult {
        return if (ethereumFeatureToggle.isFeatureEnabled) {
            val list = buildList {
                addAll(
                    claimLocalRepository.getAllBundles()
                        .map { bridgeMapper.toHistoryItem(it) },
                )
                addAll(
                    claimLocalRepository.getAllSendDetails()
                        .map { bridgeMapper.toHistoryItem(it) }
                )
            }
            HistoryPagingResult.Success(list)
        } else {
            HistoryPagingResult.Success(emptyList())
        }
    }

    override suspend fun loadNextPage(limit: Int, mintAddress: String): HistoryPagingResult {
        return HistoryPagingResult.Success(emptyList())
    }

    override suspend fun findTransactionById(id: String): HistoryTransaction? {
        val claimTransaction = claimLocalRepository.getBundle(id)
            ?.let { bridgeMapper.toHistoryItem(it) }
        val sendTransaction = claimLocalRepository.getSendDetails(id)
            ?.let { bridgeMapper.toHistoryItem(it) }

        return claimTransaction ?: sendTransaction
    }

    override fun getPagingState(mintAddress: String?): HistoryPagingState {
        return HistoryPagingState.INACTIVE
    }
}
