package org.p2p.wallet.bridge.send

import timber.log.Timber
import java.math.BigDecimal
import org.p2p.core.token.SolAddress
import org.p2p.core.wrapper.eth.EthAddress
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.model.types.Encoding
import org.p2p.solanaj.rpc.RpcSolanaRepository
import org.p2p.wallet.bridge.send.repository.EthereumSendRepository
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.sdk.facade.RelaySdkFacade
import org.p2p.wallet.utils.toBase58Instance

class BridgeSendInteractor(
    private val repository: EthereumSendRepository,
    private val ethereumKitRepository: EthereumSendRepository,
    private val tokenKeyProvider: TokenKeyProvider,
    private val relaySdkFacade: RelaySdkFacade,
    private val rpcSolanaRepository: RpcSolanaRepository
) {

    suspend fun sendTransaction(
        source: SolAddress,
        recipient: EthAddress,
        tokenMint: String,
        amount: BigDecimal
    ): String {
        val mint = SolAddress(tokenMint)
        val userPublicKey = tokenKeyProvider.publicKey
        val userWallet = SolAddress(userPublicKey)

        // todo who pay?
        val feePayer = SolAddress(userPublicKey)

        val sendTransaction = repository.transferFromSolana(
            userWallet = userWallet,
            feePayer = feePayer,
            source = source,
            recipient = recipient,
            mint = mint,
            amount = amount.toPlainString()
        )
        val userAccount = Account(tokenKeyProvider.keyPair)

        val signedTransaction = relaySdkFacade.signTransaction(
            transaction = sendTransaction.transaction.toBase58Instance(),
            keyPair = userAccount.getEncodedKeyPair().toBase58Instance(),
            // todo need it?
            recentBlockhash = null
        )
        val firstTransactionSignature = rpcSolanaRepository.sendTransaction(
            serializedTransaction = signedTransaction.transaction.base58Value,
            encoding = Encoding.BASE58
        )
        Timber.i("Send bridge transaction success: $firstTransactionSignature")
        return firstTransactionSignature
    }
}
