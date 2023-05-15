package org.p2p.wallet.bridge.claim.mapper

import org.p2p.ethereumkit.external.model.EthereumClaimToken
import org.p2p.ethereumkit.external.repository.EthereumRepository
import org.p2p.wallet.bridge.claim.model.canBeClaimed
import org.p2p.wallet.bridge.model.BridgeBundle

class EthereumBundleMapper(
    private val ethereumRepository: EthereumRepository
) {
    suspend fun mapBundle(item: BridgeBundle): EthereumClaimToken {
        val contractAddress = item.resultAmount.token ?: ethereumRepository.getAddress()
        val canBeClaimed = item.status.canBeClaimed()
        return EthereumClaimToken(
            bundleId = item.bundleId,
            contractAddress = contractAddress,
            isClaiming = !canBeClaimed
        )
    }
}
