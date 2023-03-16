package org.p2p.wallet.bridge.claim.interactor

import org.p2p.core.token.SolAddress
import org.p2p.core.wrapper.eth.EthAddress
import org.p2p.ethereumkit.external.repository.EthereumRepository
import org.p2p.wallet.bridge.claim.repository.EthereumClaimRepository
import org.p2p.wallet.bridge.model.BridgeBundle
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider

const val DEFAULT_ERC20_TOKEN_SLIPPAGE = 5

class ClaimInteractor(
    private val ethereumClaimRepository: EthereumClaimRepository,
    private val ethereumRepository: EthereumRepository,
    private val tokenKeyProvider: TokenKeyProvider
) {
    suspend fun getEthereumBundle(erc20Token: EthAddress?, amount: String): BridgeBundle {
        val solanaAddress = SolAddress(tokenKeyProvider.publicKey)
        val ethereumAddress = ethereumRepository.getAddress()

        return ethereumClaimRepository.getEthereumBundle(
            ethAddress = ethereumAddress,
            recipientAddress = solanaAddress,
            erc20Token = erc20Token,
            amount = amount,
            slippage = if (erc20Token != null) {
                DEFAULT_ERC20_TOKEN_SLIPPAGE
            } else {
                null
            }
        )
    }

    suspend fun sendEthereumBundle(bundle: BridgeBundle) {
        return ethereumClaimRepository.sendEthereumBundle(bundle)
    }
}
