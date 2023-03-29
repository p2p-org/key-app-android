package org.p2p.wallet.bridge.send

import java.math.BigDecimal
import kotlinx.coroutines.withContext
import org.p2p.core.token.SolAddress
import org.p2p.core.token.Token
import org.p2p.core.wrapper.eth.EthAddress
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.OperationType
import org.p2p.solanaj.model.types.Encoding
import org.p2p.solanaj.rpc.RpcSolanaRepository
import org.p2p.solanaj.utils.crypto.Base64String
import org.p2p.solanaj.utils.crypto.toBase64Instance
import org.p2p.wallet.bridge.send.repository.EthereumSendRepository
import org.p2p.wallet.feerelayer.interactor.FeeRelayerAccountInteractor
import org.p2p.wallet.feerelayer.model.FeeRelayerSignTransaction
import org.p2p.wallet.feerelayer.model.FeeRelayerStatistics
import org.p2p.wallet.feerelayer.repository.FeeRelayerRepository
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
    private val dispatchers: CoroutineDispatchers,
    private val rpcSolanaRepository: RpcSolanaRepository,
    private val feeRelayerRepository: FeeRelayerRepository,
    private val feeRelayerAccountInteractor: FeeRelayerAccountInteractor,
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

        val relayAccount = feeRelayerAccountInteractor.getRelayInfo()
        val feePayer = SolAddress(relayAccount.feePayerAddress.toBase58())

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
            transaction = sendTransaction.transaction,
            keyPair = userAccount.getEncodedKeyPair().toBase58Instance(),
            recentBlockhash = null
        )
        val feeRelayerTransaction = signByFeeRelayer(signedTransaction, token)
        sendToBlockchain(feeRelayerTransaction.transaction)
    }

    private suspend fun signByFeeRelayer(
        signedTransaction: RelaySdkSignedTransaction,
        token: Token.Active
    ): FeeRelayerSignTransaction {
        val statistics = FeeRelayerStatistics(
            operationType = OperationType.TRANSFER,
            currency = token.mintAddress
        )
        return feeRelayerRepository.signTransaction(
            transaction = signedTransaction.transaction.decodeToBytes().toBase64Instance(),
            statistics = statistics
        )
    }

    private suspend fun sendToBlockchain(transaction: Base64String): String {
        return rpcSolanaRepository.sendTransaction(
            serializedTransaction = transaction.base64Value,
            encoding = Encoding.BASE64
        )
    }
}
