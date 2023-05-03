package org.p2p.wallet.bridge.claim.interactor

import java.math.BigDecimal
import org.p2p.core.token.SolAddress
import org.p2p.core.wrapper.eth.EthAddress
import org.p2p.ethereumkit.external.model.EthereumClaimToken
import org.p2p.ethereumkit.internal.models.Signature
import org.p2p.wallet.bridge.claim.mapper.EthereumBundleMapper
import org.p2p.wallet.bridge.claim.repository.EthereumClaimLocalRepository
import org.p2p.wallet.bridge.claim.repository.EthereumClaimRepository
import org.p2p.wallet.bridge.model.BridgeBundle
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.transaction.model.NewShowProgress

const val DEFAULT_ERC20_TOKEN_SLIPPAGE = 15

class ClaimInteractor(
    private val ethereumClaimRepository: EthereumClaimRepository,
    private val ethereumClaimLocalRepository: EthereumClaimLocalRepository,
    private val tokenKeyProvider: TokenKeyProvider,
    private val mapper: EthereumBundleMapper,
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

    suspend fun getEthereumMinAmountForFreeFee(): BigDecimal {
        return ethereumClaimRepository.getEthereumMinAmountForFreeFee()
    }

    fun saveProgressDetails(bundleId: String, progressDetails: NewShowProgress) {
        ethereumClaimLocalRepository.saveProgressDetails(bundleId, progressDetails)
    }

    fun getProgressDetails(bundleId: String): NewShowProgress? {
        return ethereumClaimLocalRepository.getProgressDetails(bundleId)
    }
}
