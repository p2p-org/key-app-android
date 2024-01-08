package org.p2p.wallet.bridge.send.interactor

import java.math.BigDecimal
import kotlinx.coroutines.withContext
import org.p2p.core.token.SolAddress
import org.p2p.core.token.Token
import org.p2p.core.token.TokenVisibility
import org.p2p.ethereumkit.external.model.ERC20Tokens
import org.p2p.ethereumkit.external.repository.EthereumRepository
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.OperationType
import org.p2p.solanaj.model.types.Encoding
import org.p2p.solanaj.rpc.RpcSolanaRepository
import org.p2p.core.crypto.Base64String
import org.p2p.core.crypto.toBase64Instance
import org.p2p.wallet.bridge.send.model.BridgeSendFees
import org.p2p.wallet.bridge.send.model.BridgeSendTransaction
import org.p2p.wallet.bridge.send.repository.EthereumSendRepository
import org.p2p.wallet.feerelayer.model.FeeRelayerSignTransaction
import org.p2p.wallet.feerelayer.model.FeeRelayerStatistics
import org.p2p.wallet.feerelayer.repository.FeeRelayerRepository
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.sdk.facade.RelaySdkFacade
import org.p2p.wallet.sdk.facade.model.relay.RelaySdkSignedTransaction
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.core.crypto.toBase58Instance
import org.p2p.core.token.TokenExtensions
import org.p2p.core.utils.Constants
import org.p2p.core.wrapper.eth.EthAddress
import org.p2p.wallet.bridge.send.model.BridgeSendTransactionDetails
import org.p2p.wallet.tokenservice.TokenServiceCoordinator

class BridgeSendInteractor(
    private val ethereumSendRepository: EthereumSendRepository,
    private val ethereumRepository: EthereumRepository,
    private val userInteractor: UserInteractor,
    private val tokenServiceCoordinator: TokenServiceCoordinator,
    private val tokenKeyProvider: TokenKeyProvider,
    private val relaySdkFacade: RelaySdkFacade,
    private val dispatchers: CoroutineDispatchers,
    private val rpcSolanaRepository: RpcSolanaRepository,
    private val feeRelayerRepository: FeeRelayerRepository
) {

    suspend fun getSendFee(
        sendTokenMint: SolAddress?,
        amount: String
    ): BridgeSendFees {
        val userAddress = SolAddress(tokenKeyProvider.publicKey)
        val ethereumAddress = ethereumRepository.getAddress()
        return ethereumSendRepository.getSendFee(userAddress, ethereumAddress, sendTokenMint, amount)
    }

    suspend fun supportedSendTokens(): List<Token.Active> {
        val supportedTokensMints = ERC20Tokens.values().map { it.mintAddress }
        return tokenServiceCoordinator.getUserTokens()
            .filter { !it.isZero && it.mintAddress in supportedTokensMints }
            .sortedWith(BridgeTokenComparator())
            .ifEmpty {
                // TODO PWN-7613 also block button as we can't send we do not have funds
                val usdCet = userInteractor.findTokenDataByAddress(ERC20Tokens.USDC.mintAddress) as Token.Other
                listOf(toTokenActiveStub(usdCet))
            }
    }

    private fun toTokenActiveStub(token: Token.Other): Token.Active {
        return Token.Active(
            publicKey = token.publicKey.orEmpty(),
            totalInUsd = BigDecimal.ZERO,
            total = BigDecimal.ZERO,
            tokenSymbol = token.tokenSymbol,
            decimals = token.decimals,
            mintAddress = token.mintAddress,
            tokenName = token.tokenName,
            iconUrl = token.iconUrl,
            rate = null,
            visibility = TokenVisibility.DEFAULT,
            isWrapped = token.isWrapped,
            tokenExtensions = TokenExtensions(),
            tokenServiceAddress = Constants.TOKEN_SERVICE_NATIVE_SOL_TOKEN
        )
    }

    suspend fun sendTransaction(
        token: Token.Active,
        sendTransaction: BridgeSendTransaction
    ): String = withContext(dispatchers.io) {

        val userAccount = Account(tokenKeyProvider.keyPair)

        val signedTransaction = relaySdkFacade.signTransaction(
            transaction = sendTransaction.transaction,
            keyPair = userAccount.getEncodedKeyPair().toBase58Instance(),
            recentBlockhash = null
        )
        val feeRelayerTransaction = signByFeeRelayer(signedTransaction, token)
        sendToBlockchain(feeRelayerTransaction.transaction)
    }

    suspend fun transferFromSolana(
        userWallet: SolAddress,
        feePayer: SolAddress,
        source: SolAddress,
        recipient: EthAddress,
        tokenMint: SolAddress?,
        amount: String
    ): BridgeSendTransaction {
        return ethereumSendRepository.transferFromSolana(
            userWallet = userWallet,
            feePayer = feePayer,
            source = source,
            recipient = recipient,
            mint = tokenMint,
            amount = amount
        )
    }

    suspend fun getSendTransactionDetails(message: String): BridgeSendTransactionDetails {
        return ethereumSendRepository.getSendTransactionDetail(message)
    }

    suspend fun getSendTransactionDetails(userWallet: SolAddress): List<BridgeSendTransactionDetails> {
        return ethereumSendRepository.getSendTransactionsDetail(userWallet)
    }

    suspend fun getSendFee(
        userWallet: SolAddress,
        recipient: EthAddress,
        mint: SolAddress?,
        amount: String
    ): BridgeSendFees {
        return ethereumSendRepository.getSendFee(
            userWallet = userWallet,
            recipient = recipient,
            mint = mint,
            amount = amount
        )
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
            transaction = signedTransaction.transaction
                .decodeToBytes()
                .toBase64Instance(),
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
