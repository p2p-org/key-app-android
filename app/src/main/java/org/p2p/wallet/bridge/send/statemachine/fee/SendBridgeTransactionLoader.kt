package org.p2p.wallet.bridge.send.statemachine.fee

import java.math.BigDecimal
import java.math.BigInteger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.p2p.core.token.SolAddress
import org.p2p.core.token.Token
import org.p2p.core.utils.isZero
import org.p2p.core.utils.orZero
import org.p2p.core.utils.toLamports
import org.p2p.wallet.bridge.send.interactor.BridgeSendInteractor
import org.p2p.wallet.bridge.send.model.BridgeSendTransaction
import org.p2p.wallet.bridge.send.model.getFeeList
import org.p2p.wallet.bridge.send.repository.EthereumSendRepository
import org.p2p.wallet.bridge.send.statemachine.SendFeatureException
import org.p2p.wallet.bridge.send.statemachine.SendState
import org.p2p.wallet.bridge.send.statemachine.bridgeFee
import org.p2p.wallet.bridge.send.statemachine.bridgeToken
import org.p2p.wallet.bridge.send.statemachine.inputAmount
import org.p2p.wallet.bridge.send.statemachine.mapper.SendBridgeStaticStateMapper
import org.p2p.wallet.bridge.send.statemachine.model.SendFee
import org.p2p.wallet.bridge.send.statemachine.model.SendInitialData
import org.p2p.wallet.bridge.send.statemachine.model.SendToken
import org.p2p.wallet.bridge.send.statemachine.validator.SendBridgeValidator
import org.p2p.wallet.feerelayer.interactor.FeeRelayerAccountInteractor
import org.p2p.wallet.feerelayer.model.TransactionFeeLimits
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider

class SendBridgeTransactionLoader constructor(
    private val initialData: SendInitialData.Bridge,
    private val mapper: SendBridgeStaticStateMapper,
    private val validator: SendBridgeValidator,
    private val bridgeSendInteractor: BridgeSendInteractor,
    private val feeRelayerAccountInteractor: FeeRelayerAccountInteractor,
    private val repository: EthereumSendRepository,
    private val tokenKeyProvider: TokenKeyProvider,
) {

    private var freeTransactionFeeLimits: TransactionFeeLimits? = null

    fun prepareTransaction(
        lastStaticState: SendState.Static,
        lastAmount: BigDecimal? = null
    ): Flow<SendState> = flow {

        val token = lastStaticState.bridgeToken ?: return@flow

        emit(SendState.Loading.Fee(lastStaticState))
        val lastFeeInToken = getFeeTotalInToken(lastStaticState)
        val currentAmount = lastAmount ?: lastStaticState.inputAmount.orZero()
        val finalAmount = if (currentAmount.isZero()) {
            currentAmount
        } else {
            currentAmount + lastFeeInToken
        }
        val fee = loadFee(token, finalAmount)
        val updatedFee = mapper.updateFee(lastStaticState, fee)
        val inputAmount = updatedFee.inputAmount
        if (inputAmount != null) {
            val amountWithFee = inputAmount + lastFeeInToken
            val inputLamports = amountWithFee.toLamports(token.token.decimals)
            validator.validateIsFeeMoreThanTotal(token, fee, inputAmount)
            validator.validateIsFeeMoreThanAmount(fee, inputAmount, amountWithFee)
            val sendTransaction = createTransaction(token.token, inputLamports)
            emit(SendState.Static.ReadyToSend(token, fee, inputAmount, sendTransaction))
        } else {
            emit(SendState.Static.TokenZero(token, fee))
            validator.validateIsFeeMoreThanTotal(token, fee, currentAmount)
        }
    }

    private fun getFeeTotalInToken(lastStaticState: SendState.Static): BigDecimal {
        return lastStaticState.bridgeFee?.fee.getFeeList().sumOf { it.amountInToken }
    }

    private suspend fun loadFee(
        bridgeToken: SendToken.Bridge,
        amount: BigDecimal,
    ): SendFee.Bridge {
        return try {
            val token = bridgeToken.token

            val sendTokenMint = if (token.isSOL) {
                null
            } else {
                SolAddress(token.mintAddress)
            }

            val formattedAmount = amount.toLamports(token.decimals)

            if (freeTransactionFeeLimits == null) {
                freeTransactionFeeLimits = feeRelayerAccountInteractor.getFreeTransactionFeeLimit()
                feeRelayerAccountInteractor.getUserRelayAccount(useCache = false)
            }

            val fee = bridgeSendInteractor.getSendFee(
                sendTokenMint = sendTokenMint,
                amount = formattedAmount.toString()
            )

            SendFee.Bridge(
                fee = fee,
                feeLimitInfo = freeTransactionFeeLimits
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: SendFeatureException) {
            throw e
        } catch (e: Throwable) {
            throw SendFeatureException.FeeLoadingError(e.message, amount)
        }
    }

    private suspend fun createTransaction(
        token: Token.Active,
        amountInLamports: BigInteger
    ): BridgeSendTransaction {
        val tokenMint = token.mintAddress
        val userPublicKey = tokenKeyProvider.publicKey
        val userWallet = SolAddress(userPublicKey)

        val relayAccount = feeRelayerAccountInteractor.getRelayInfo()
        val feePayer = SolAddress(relayAccount.feePayerAddress.toBase58())

        return repository.transferFromSolana(
            userWallet = userWallet,
            feePayer = feePayer,
            source = SolAddress(token.publicKey),
            recipient = initialData.recipient,
            mint = SolAddress(tokenMint),
            amount = amountInLamports.toString()
        )
    }
}
