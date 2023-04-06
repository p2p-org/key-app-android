package org.p2p.wallet.bridge.interactor

import java.math.BigDecimal
import java.math.BigInteger
import org.p2p.core.token.Token
import org.p2p.core.wrapper.HexString
import org.p2p.core.wrapper.eth.EthAddress
import org.p2p.ethereumkit.external.model.EthereumClaimToken
import org.p2p.ethereumkit.external.repository.EthereumRepository
import org.p2p.ethereumkit.internal.models.Signature
import org.p2p.wallet.bridge.claim.interactor.ClaimInteractor
import org.p2p.wallet.bridge.model.BridgeBundle
import org.p2p.wallet.bridge.send.BridgeSendInteractor

class EthereumInteractor(
    private val claimInteractor: ClaimInteractor,
    private val sendInteractor: BridgeSendInteractor,
    private val ethereumRepository: EthereumRepository,
) {

    fun setup(userSeedPhrase: List<String>) {
        ethereumRepository.init(userSeedPhrase)
    }

    suspend fun getWalletBalance(): BigInteger {
        return ethereumRepository.getBalance()
    }

    suspend fun getPriceForToken(tokenAddress: String): BigDecimal {
        return ethereumRepository.getPriceForToken(tokenAddress)
    }

    suspend fun loadWalletTokens(ethereumBundleStatuses: List<EthereumClaimToken>): List<Token.Eth> {
        return ethereumRepository.loadWalletTokens(ethereumBundleStatuses)
    }

    suspend fun getEthAddress(): EthAddress {
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

    suspend fun getListOfEthereumBundleStatuses(): List<EthereumClaimToken> {
        val ethereumAddress: EthAddress = ethereumRepository.getAddress()
        return claimInteractor.getListOfEthereumBundleStatuses(ethereumAddress)
    }

    suspend fun sendTransaction(
        recipient: EthAddress,
        token: Token.Active,
        amountInLamports: BigInteger,
    ): String {
        return sendInteractor.sendTransaction(
            recipient = recipient,
            token = token,
            amountInLamports = amountInLamports
        )
    }

    fun signClaimTransaction(transaction: HexString): Signature {
        return ethereumRepository.signTransaction(transaction)
    }

    suspend fun sendClaimBundle(signatures: List<Signature>) {
        return claimInteractor.sendEthereumBundle(signatures)
    }
}
