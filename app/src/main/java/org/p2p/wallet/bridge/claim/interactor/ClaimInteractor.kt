package org.p2p.wallet.bridge.claim.interactor

import org.p2p.core.token.SolAddress
import org.p2p.ethereumkit.external.repository.EthereumRepository
import org.p2p.ethereumkit.internal.models.EthAddress
import org.p2p.wallet.bridge.claim.repository.EthereumClaimRepository
import org.p2p.wallet.bridge.model.BridgeBundle
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider

class ClaimInteractor(
    private val ethereumClaimRepository: EthereumClaimRepository,
    private val ethereumRepository: EthereumRepository,
    private val tokenKeyProvider: TokenKeyProvider
) {
    suspend fun getEthereumBundle(erc20TokenAddress: String, amount: String): BridgeBundle {
        val ethereumAddress = ethereumRepository.getAddress()
        val solanaAddress = SolAddress(tokenKeyProvider.publicKey)

        return ethereumClaimRepository.getEthereumBundle(
            ethAddress = ethereumAddress,
            recipientAddress = solanaAddress,
            erc20Token = EthAddress(erc20TokenAddress),
            amount = amount,
            slippage = 5
        )
    }
}
