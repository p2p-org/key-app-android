package org.p2p.wallet.bridge.claim.mapper

import org.p2p.ethereumkit.external.model.EthereumClaimToken
import org.p2p.wallet.bridge.model.BridgeBundle

class EthereumModuleMapper {

    fun mapBundle(item: BridgeBundle): EthereumClaimToken? {
        val contractAddress = item.resultAmount.token ?: return null
        val isClaiming = item.status?.canBeClaimed() == false
        return EthereumClaimToken(
            contractAddress = contractAddress,
            isClaiming = isClaiming
        )
    }
}
