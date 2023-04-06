package org.p2p.wallet.bridge.send.statemachine.fee

import java.math.BigDecimal
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.p2p.core.token.SolAddress
import org.p2p.core.utils.orZero
import org.p2p.core.utils.toLamports
import org.p2p.wallet.bridge.send.interactor.BridgeSendInteractor
import org.p2p.wallet.bridge.send.statemachine.SendFeatureException
import org.p2p.wallet.bridge.send.statemachine.SendState
import org.p2p.wallet.bridge.send.statemachine.bridgeToken
import org.p2p.wallet.bridge.send.statemachine.inputAmount
import org.p2p.wallet.bridge.send.statemachine.mapper.SendBridgeStaticStateMapper
import org.p2p.wallet.bridge.send.statemachine.model.SendFee
import org.p2p.wallet.bridge.send.statemachine.model.SendInitialData
import org.p2p.wallet.bridge.send.statemachine.model.SendToken
import org.p2p.wallet.bridge.send.statemachine.validator.SendBridgeValidator
import org.p2p.wallet.feerelayer.interactor.FeeRelayerAccountInteractor
import org.p2p.wallet.feerelayer.model.FeePayerSelectionStrategy
import org.p2p.wallet.feerelayer.model.TransactionFeeLimits

class SendBridgeFeeLoader constructor(
    private val initialData: SendInitialData.Bridge,
    private val mapper: SendBridgeStaticStateMapper,
    private val validator: SendBridgeValidator,
    private val bridgeSendInteractor: BridgeSendInteractor,
    private val feeRelayerAccountInteractor: FeeRelayerAccountInteractor,
    private val feeRelayerCounter: SendBridgeFeeRelayerCounter,
) {

    private var freeTransactionFeeLimits: TransactionFeeLimits? = null

    fun updateFee(
        lastStaticState: SendState.Static
    ): Flow<SendState> = flow {

        val token = lastStaticState.bridgeToken ?: return@flow

        emit(SendState.Loading.Fee(lastStaticState))
        val fee = loadFee(token, lastStaticState.inputAmount.orZero())
        val updatedFee = mapper.updateFee(lastStaticState, fee)
        emit(updatedFee)
        validator.validateIsFeeMoreThenAmount(lastStaticState, fee)
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

            feeRelayerCounter.calculateFeeForPayer(
                sourceToken = token,
                feePayerToken = token,
                recipient = initialData.recipient.hex,
                strategy = FeePayerSelectionStrategy.SELECT_FEE_PAYER,
                tokenAmount = amount,
                bridgeFees = fee,
            )

            SendFee.Bridge(
                fee = fee,
                tokenToPayFee = feeRelayerCounter.tokenToPayFee,
                feeRelayerFee = feeRelayerCounter.feeRelayerFee,
                feeLimitInfo = freeTransactionFeeLimits
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            throw SendFeatureException.FeeLoadingError(e.message)
        }
    }
}
