package org.p2p.wallet.bridge.interactor

import java.math.BigDecimal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.p2p.core.token.SolAddress
import org.p2p.core.token.Token
import org.p2p.core.wrapper.HexString
import org.p2p.core.wrapper.eth.EthAddress
import org.p2p.ethereumkit.external.model.EthereumClaimToken
import org.p2p.ethereumkit.external.repository.EthereumRepository
import org.p2p.ethereumkit.internal.models.Signature
import org.p2p.wallet.bridge.claim.interactor.ClaimInteractor
import org.p2p.wallet.bridge.model.BridgeBundle
import org.p2p.wallet.bridge.send.model.BridgeSendTransactionDetails
import org.p2p.wallet.bridge.send.repository.EthereumSendRepository
import org.p2p.wallet.common.feature_toggles.toggles.remote.EthAddressEnabledFeatureToggle
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider

class EthereumInteractor(
    private val tokenKeyProvider: TokenKeyProvider,
    private val claimInteractor: ClaimInteractor,
    private val ethereumRepository: EthereumRepository,
    private val ethereumSendRepository: EthereumSendRepository,
    private val ethAddressEnabledFeatureToggle: EthAddressEnabledFeatureToggle,
) {

    fun setup(userSeedPhrase: List<String>) {
        ethereumRepository.init(userSeedPhrase)
    }

    suspend fun loadWalletTokens() {
        if (ethAddressEnabledFeatureToggle.isFeatureEnabled) {
            val bundles = getListOfEthereumBundleStatuses()
            ethereumSendRepository.getSendTransactionsDetail(SolAddress(tokenKeyProvider.publicKey))
            ethereumRepository.loadWalletTokens(bundles)
        }
    }

    fun getTokensFlow(): Flow<List<Token.Eth>> {
        return if (ethAddressEnabledFeatureToggle.isFeatureEnabled) {
            ethereumRepository.getWalletTokensFlow()
        } else {
            flowOf(emptyList())
        }
    }

    fun getEthAddress(): EthAddress {
        return ethereumRepository.getAddress()
    }

    suspend fun getEthereumBundle(erc20Token: EthAddress?, amount: String): BridgeBundle {
        val ethereumAddress: EthAddress = ethereumRepository.getAddress()
        return claimInteractor.getEthereumBundle(
            erc20Token = erc20Token,
            amount = amount,
            ethereumAddress = ethereumAddress
        )
    }

    private suspend fun getListOfEthereumBundleStatuses(): List<EthereumClaimToken> {
        val ethereumAddress: EthAddress = ethereumRepository.getAddress()
        return claimInteractor.getListOfEthereumBundleStatuses(ethereumAddress)
    }

    fun signClaimTransaction(transaction: HexString): Signature {
        return ethereumRepository.signTransaction(transaction)
    }

    suspend fun sendClaimBundle(signatures: List<Signature>) {
        return claimInteractor.sendEthereumBundle(signatures)
    }

    suspend fun getClaimMinAmountForFreeFee(): BigDecimal {
        return claimInteractor.getEthereumMinAmountForFreeFee()
    }

    fun getClaimBundleById(bundleId: String): BridgeBundle? {
        return claimInteractor.getClaimBundleById(bundleId)
    }

    fun getSendBundleById(bundleId: String): BridgeSendTransactionDetails? {
        return claimInteractor.getSendBundleById(bundleId)
    }
}
