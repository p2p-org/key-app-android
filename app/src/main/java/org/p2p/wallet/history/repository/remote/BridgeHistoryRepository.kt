package org.p2p.wallet.history.repository.remote

import org.p2p.core.utils.Constants
import org.p2p.wallet.bridge.api.mapper.BridgeMapper
import org.p2p.wallet.bridge.claim.repository.EthereumBridgeLocalRepository
import org.p2p.wallet.bridge.model.BridgeBundle
import org.p2p.wallet.bridge.send.model.BridgeSendTransactionDetails
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
                    claimLocalRepository.getAllClaimBundles()
                        .filter(BridgeBundle::isProcessing)
                        .mapNotNull { bridgeMapper.toHistoryItem(it, mintAddress) },
                )
                addAll(
                    claimLocalRepository.getAllSendDetails()
                        .filter(BridgeSendTransactionDetails::isInProgress)
                        .mapNotNull { bridgeMapper.toHistoryItem(it, mintAddress) }
                )
            }
            HistoryPagingResult.Success(list)
        } else {
            HistoryPagingResult.Success(emptyList())
        }
    }

    override suspend fun loadNextPage(limit: Int, mintAddress: String): HistoryPagingResult {
        return loadHistory(limit, mintAddress)
    }

    override suspend fun findTransactionById(id: String): HistoryTransaction? {
        val claimTransaction = claimLocalRepository.getClaimBundleById(id)
            ?.let { bridgeMapper.toHistoryItem(claimBundle = it, mintAddress = Constants.WRAPPED_SOL_MINT) }
        val sendTransaction = claimLocalRepository.getSendDetails(id)
            ?.let { bridgeMapper.toHistoryItem(sendDetails = it, mintAddress = Constants.WRAPPED_SOL_MINT) }

        return claimTransaction ?: sendTransaction
    }

    override fun getPagingState(mintAddress: String?): HistoryPagingState {
        return HistoryPagingState.ACTIVE
    }
}
