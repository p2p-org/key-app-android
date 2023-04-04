package org.p2p.wallet.bridge.claim.interactor

import org.p2p.core.token.SolAddress
import org.p2p.core.wrapper.eth.EthAddress
import org.p2p.ethereumkit.external.model.EthereumClaimToken
import org.p2p.ethereumkit.internal.models.Signature
import org.p2p.wallet.bridge.claim.mapper.EthereumModuleMapper
import org.p2p.wallet.bridge.claim.repository.EthereumClaimRepository
import org.p2p.wallet.bridge.model.BridgeBundle
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider

const val DEFAULT_ERC20_TOKEN_SLIPPAGE = 15

class ClaimInteractor(
    private val ethereumClaimRepository: EthereumClaimRepository,
    private val tokenKeyProvider: TokenKeyProvider,
    private val mapper: EthereumModuleMapper,
) {
    suspend fun getEthereumBundle(
        erc20Token: EthAddress?,
        amount: String,
        ethereumAddress: EthAddress,
    ): BridgeBundle {
        val solanaAddress = SolAddress(tokenKeyProvider.publicKey)

        return ethereumClaimRepository.getEthereumBundle(
            ethAddress = ethereumAddress,
            recipientAddress = solanaAddress,
            erc20Token = erc20Token,
            amount = amount,
            slippage = DEFAULT_ERC20_TOKEN_SLIPPAGE
        )
    }

    suspend fun sendEthereumBundle(signatures: List<Signature>) {
        return ethereumClaimRepository.sendEthereumBundle(signatures)
    }

    suspend fun getListOfEthereumBundleStatuses(ethereumAddress: EthAddress): List<EthereumClaimToken> {
        return ethereumClaimRepository.getListOfEthereumBundleStatuses(ethereumAddress)
            .map {
                mapper.mapBundle(it)
            }
    }
}
