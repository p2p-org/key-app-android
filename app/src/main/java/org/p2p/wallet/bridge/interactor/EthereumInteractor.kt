package org.p2p.wallet.bridge.interactor

import java.math.BigDecimal
import kotlinx.coroutines.flow.Flow
import org.p2p.core.token.SolAddress
import org.p2p.core.token.Token
import org.p2p.core.wrapper.HexString
import org.p2p.core.wrapper.eth.EthAddress
import org.p2p.ethereumkit.external.model.EthereumClaimToken
import org.p2p.ethereumkit.external.repository.EthereumRepository
import org.p2p.ethereumkit.internal.core.EthereumKit
import org.p2p.ethereumkit.internal.models.Signature
import org.p2p.wallet.bridge.claim.interactor.ClaimInteractor
import org.p2p.wallet.bridge.model.BridgeBundle
import org.p2p.wallet.bridge.send.interactor.BridgeSendInteractor
import org.p2p.wallet.bridge.send.model.BridgeSendTransactionDetails
import org.p2p.wallet.common.feature_toggles.toggles.remote.EthAddressEnabledFeatureToggle
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider

class EthereumInteractor(
    private val tokenKeyProvider: TokenKeyProvider,
    private val claimInteractor: ClaimInteractor,
    private val ethereumRepository: EthereumRepository,
    private val bridgeSendInteractor: BridgeSendInteractor
) {

    fun setup(userSeedPhrase: List<String>) {
        EthereumKit.init()
        ethereumRepository.init(userSeedPhrase)
    }

    suspend fun loadWalletTokens(claimTokens: List<EthereumClaimToken>) {
        ethereumRepository.loadWalletTokens(claimTokens)
    }

    suspend fun loadEthereumClaimTokens(): List<EthereumClaimToken> {
        return claimInteractor.getEthereumClaimTokens(getEthAddress())
    }

    suspend fun loadEthereumSendTransactionDetails() {
        bridgeSendInteractor.getSendTransactionDetails(SolAddress(tokenKeyProvider.publicKey))
    }

    fun observeTokensFlow(): Flow<List<Token.Eth>> {
        return ethereumRepository.getWalletTokensFlow()
    }

    fun getEthAddress(): EthAddress = ethereumRepository.getAddress()

    suspend fun getEthereumBundle(erc20Token: EthAddress?, amount: String): BridgeBundle {
        val ethereumAddress: EthAddress = ethereumRepository.getAddress()
        return claimInteractor.getEthereumClaimableToken(
            erc20Token = erc20Token,
            amount = amount,
            ethereumAddress = ethereumAddress
        )
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
