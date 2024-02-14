package org.p2p.wallet.striga.offramp.withdraw.interactor

import org.threeten.bp.ZonedDateTime
import timber.log.Timber
import java.math.BigDecimal
import java.util.UUID
import org.p2p.core.crypto.Base58String
import org.p2p.core.crypto.toBase58Instance
import org.p2p.core.token.Token
import org.p2p.core.utils.Constants
import org.p2p.core.utils.toLamports
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.OperationType
import org.p2p.solanaj.model.types.ConfirmationStatus
import org.p2p.wallet.feerelayer.interactor.FeeRelayerViaLinkInteractor
import org.p2p.wallet.feerelayer.model.FeeRelayerStatistics
import org.p2p.wallet.history.interactor.HistoryInteractor
import org.p2p.wallet.history.model.rpc.RpcHistoryAmount
import org.p2p.wallet.history.model.rpc.RpcHistoryTransaction
import org.p2p.wallet.history.model.rpc.RpcHistoryTransactionType
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.infrastructure.transactionmanager.TransactionManager
import org.p2p.wallet.striga.wallet.interactor.StrigaWalletInteractor
import org.p2p.wallet.striga.wallet.models.StrigaUserBankingDetails
import org.p2p.wallet.striga.wallet.models.ids.StrigaWithdrawalChallengeId
import org.p2p.wallet.transaction.model.HistoryTransactionStatus
import org.p2p.wallet.user.interactor.UserTokensInteractor
import org.p2p.wallet.utils.toPublicKey

class StrigaWithdrawInteractor(
    private val strigaWalletInteractor: StrigaWalletInteractor,
    private val userTokensInteractor: UserTokensInteractor,
    private val tokenKeyProvider: TokenKeyProvider,
    private val feeRelayerLinkInteractor: FeeRelayerViaLinkInteractor,
    private val transactionBuilder: StrigaWithdrawSendTransactionBuilder,
    private val transactionManager: TransactionManager,
    private val historyInteractor: HistoryInteractor
) {

    suspend fun getUserEurBankingDetails(): StrigaUserBankingDetails {
        return strigaWalletInteractor.getEurBankingDetails()
    }

    suspend fun saveUpdatedEurBankingDetails(iban: String, bic: String) {
        strigaWalletInteractor.saveNewEurBankingDetails(iban, bic)
    }

    suspend fun withdrawEur(eurAmount: BigDecimal): StrigaWithdrawalChallengeId {
        return strigaWalletInteractor.initEurOffRamp(eurAmount).challengeId
    }

    suspend fun withdrawUsdc(amount: BigDecimal): Pair<String, Token.Active> {
        feeRelayerLinkInteractor.load()

        val userUsdcToken = userTokensInteractor.findUserToken(Constants.USDC_MINT)
            ?: throw StrigaWithdrawError.StrigaSendUsdcFailed("USDC token not found")

        return UUID.randomUUID().toString() to userUsdcToken
//        val userStrigaUsdcAddress = strigaWalletInteractor.getCryptoAccountDetails().depositAddress
//
//        val internalTransactionId = UUID.randomUUID().toString()
//        try {
//            val transactionId = sendUsdcToStriga(userUsdcToken, userStrigaUsdcAddress, amount)
//            val pendingTransaction = addPendingTransaction(
//                transactionId = transactionId,
//                usdcToken = userUsdcToken,
//                amount = amount,
//                userStrigaUsdcAddress = userStrigaUsdcAddress
//            )
//            transactionManager.emitTransactionState(
//                transactionId = internalTransactionId,
//                state = SendSwapProgressState.Success(pendingTransaction, userUsdcToken.tokenSymbol)
//            )
//        } catch (error: Throwable) {
//            transactionManager.emitTransactionState(
//                transactionId = internalTransactionId,
//                state = SendSwapProgressState.Error(internalTransactionId)
//            )
//            throw error
//        }
//        return internalTransactionId to userUsdcToken
    }

    @Throws(StrigaWithdrawError.StrigaSendUsdcFailed::class)
    private suspend fun sendUsdcToStriga(
        usdcToken: Token.Active,
        strigaAddress: String,
        amountToSend: BigDecimal
    ): Base58String {
        val userAccount = Account(tokenKeyProvider.keyPair)

        val txSignature = try {
            val preparedTransaction = transactionBuilder.buildSendTransaction(
                userAccount = userAccount,
                destinationAddress = strigaAddress.toPublicKey(),
                token = usdcToken,
                amountToSend = amountToSend.toLamports(usdcToken.decimals)
            )

            feeRelayerLinkInteractor.signAndSendTransaction(
                preparedTransaction = preparedTransaction,
                statistics = FeeRelayerStatistics(
                    operationType = OperationType.TRANSFER,
                    currency = usdcToken.mintAddress
                ),
                isRetryEnabled = false,
                isSimulation = false,
                preflightCommitment = ConfirmationStatus.FINALIZED
            )
        } catch (error: Throwable) {
            Timber.i(error)
            throw StrigaWithdrawError.StrigaSendUsdcFailed("failed to send transaction", error)
        }

        Timber.i("Send success, tx = $txSignature")
        return Base58String(txSignature)
    }

    private fun buildSendPendingTransaction(
        transactionId: Base58String,
        token: Token.Active,
        amountSent: BigDecimal,
        recipientAddress: String
    ): RpcHistoryTransaction.Transfer {
        return RpcHistoryTransaction.Transfer(
            signature = transactionId.base58Value,
            date = ZonedDateTime.now(),
            blockNumber = -1,
            type = RpcHistoryTransactionType.SEND,
            senderAddress = tokenKeyProvider.publicKey,
            amount = RpcHistoryAmount(
                total = amountSent,
                totalInUsd = null
            ),
            destination = recipientAddress,
            fees = null,
            status = HistoryTransactionStatus.PENDING,
            iconUrl = token.iconUrl,
            symbol = token.tokenSymbol,
            decimals = token.decimals,
            counterPartyUsername = null
        )
    }

    private suspend fun addPendingTransaction(
        transactionId: Base58String,
        usdcToken: Token.Active,
        amount: BigDecimal,
        userStrigaUsdcAddress: String
    ): RpcHistoryTransaction.Transfer {
        val pendingTransaction = buildSendPendingTransaction(
            transactionId = transactionId,
            token = usdcToken,
            amountSent = amount,
            recipientAddress = userStrigaUsdcAddress
        )
        historyInteractor.addPendingTransaction(
            txSignature = transactionId.base58Value,
            transaction = pendingTransaction,
            mintAddress = usdcToken.mintAddress.toBase58Instance()
        )
        return pendingTransaction
    }
}
