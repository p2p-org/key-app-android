package org.p2p.wallet.bridge.send

import timber.log.Timber
import java.math.BigDecimal
import java.math.BigInteger
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.p2p.core.token.SolAddress
import org.p2p.core.token.Token
import org.p2p.core.wrapper.eth.EthAddress
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.FeeAmount
import org.p2p.solanaj.core.OperationType
import org.p2p.solanaj.core.PreparedTransaction
import org.p2p.solanaj.core.Transaction
import org.p2p.solanaj.model.types.Encoding
import org.p2p.solanaj.rpc.RpcSolanaRepository
import org.p2p.wallet.bridge.send.repository.EthereumSendRepository
import org.p2p.wallet.feerelayer.interactor.FeeRelayerInteractor
import org.p2p.wallet.feerelayer.model.FeeRelayerStatistics
import org.p2p.wallet.feerelayer.model.TokenAccount
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.sdk.facade.RelaySdkFacade
import org.p2p.wallet.sdk.facade.model.relay.RelaySdkSignedTransaction
import org.p2p.wallet.utils.toBase58Instance

class BridgeSendInteractor(
    private val repository: EthereumSendRepository,
    private val ethereumKitRepository: EthereumSendRepository,
    private val tokenKeyProvider: TokenKeyProvider,
    private val relaySdkFacade: RelaySdkFacade,
    private val feeRelayerInteractor: FeeRelayerInteractor,
    private val dispatchers: CoroutineDispatchers,
    private val rpcSolanaRepository: RpcSolanaRepository
) {

    suspend fun sendTransaction(
        source: SolAddress,
        recipient: EthAddress,
        token: Token.Active,
        amount: BigDecimal
    ): String = withContext(dispatchers.io) {
        val tokenMint = token.mintAddress
        val userPublicKey = tokenKeyProvider.publicKey
        val userWallet = SolAddress(userPublicKey)

        // todo who pay?
        val feePayer = SolAddress(userPublicKey)

        val sendTransaction = repository.transferFromSolana(
            userWallet = userWallet,
            feePayer = feePayer,
            source = source,
            recipient = recipient,
            mint = SolAddress(tokenMint),
            amount = amount.toPlainString()
        )
        val userAccount = Account(tokenKeyProvider.keyPair)

        val signedTransaction = relaySdkFacade.signTransaction(
            transaction = sendTransaction.transaction.toBase58Instance(),
            keyPair = userAccount.getEncodedKeyPair().toBase58Instance(),
            recentBlockhash = null
        )
        val sendToFeeRelayerJob = async { sendToFeeRelayer(tokenMint, token) }
        val firstTransactionSignature = sendToBlockchain(signedTransaction)
        sendToFeeRelayerJob.await()
        Timber.i("Send bridge transaction success: $firstTransactionSignature")
        firstTransactionSignature
    }

    private suspend fun sendToFeeRelayer(tokenMint: String, token: Token.Active) {
        val signers = Account(tokenKeyProvider.keyPair)
        val preparedTransaction = PreparedTransaction(
            transaction = Transaction().apply {
                sign(signers)
            },
            signers = listOf(signers),
            expectedFee = FeeAmount(),
        )

        val statistics = FeeRelayerStatistics(
            operationType = OperationType.TRANSFER,
            currency = tokenMint
        )
        feeRelayerInteractor.topUpAndRelayTransaction(
            preparedTransaction = preparedTransaction,
            payingFeeToken = TokenAccount(token.publicKey, token.mintAddress),
            additionalPaybackFee = BigInteger.ZERO,
            statistics = statistics
        )
            .firstOrNull()
            .orEmpty()
    }

    private suspend fun sendToBlockchain(signedTransaction: RelaySdkSignedTransaction): String {
        return rpcSolanaRepository.sendTransaction(
            serializedTransaction = signedTransaction.transaction.base58Value,
            encoding = Encoding.BASE58
        )
    }
}
