package org.p2p.wallet.bridge.claim.mapper

import org.p2p.ethereumkit.external.model.EthereumClaimToken
import org.p2p.ethereumkit.external.repository.EthereumRepository
import org.p2p.wallet.bridge.model.BridgeBundle

class EthereumModuleMapper(
    private val ethereumRepository: EthereumRepository
) {

    suspend fun mapBundle(item: BridgeBundle?): EthereumClaimToken {
        val contractAddress = item?.resultAmount?.token ?: ethereumRepository.getAddress()
        val isClaiming = item?.status?.canBeClaimed() ?: false
        return EthereumClaimToken(
            contractAddress = contractAddress,
            isClaiming = isClaiming
        )
    }
}
