package org.p2p.wallet.bridge.send.statemachine.handler.bridge

import java.math.BigDecimal
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.p2p.core.utils.isZero
import org.p2p.wallet.bridge.send.model.getFeeList
import org.p2p.wallet.bridge.send.statemachine.SendActionHandler
import org.p2p.wallet.bridge.send.statemachine.SendFeatureAction
import org.p2p.wallet.bridge.send.statemachine.SendState
import org.p2p.wallet.bridge.send.statemachine.bridgeFee
import org.p2p.wallet.bridge.send.statemachine.bridgeToken
import org.p2p.wallet.bridge.send.statemachine.fee
import org.p2p.wallet.bridge.send.statemachine.fee.SendBridgeTransactionLoader
import org.p2p.wallet.bridge.send.statemachine.lastStaticState
import org.p2p.wallet.bridge.send.statemachine.mapper.SendBridgeStaticStateMapper
import org.p2p.wallet.bridge.send.statemachine.validator.SendBridgeValidator

class AmountChangeActionHandler(
    private val mapper: SendBridgeStaticStateMapper,
    private val validator: SendBridgeValidator,
    private val transactionLoader: SendBridgeTransactionLoader,
) : SendActionHandler {

    override fun canHandle(newEvent: SendFeatureAction, staticState: SendState.Static): Boolean =
        newEvent is SendFeatureAction.AmountChange ||
            newEvent is SendFeatureAction.MaxAmount ||
            newEvent is SendFeatureAction.ZeroAmount

    override fun handle(
        currentState: SendState,
        newAction: SendFeatureAction
    ): Flow<SendState> = flow {
        val lastStaticState = currentState.lastStaticState
        val token = lastStaticState.bridgeToken ?: return@flow
        val feeTotalAmount = getFeeTotalInToken(lastStaticState)
        val newAmount = when (newAction) {
            is SendFeatureAction.AmountChange -> newAction.amount
            SendFeatureAction.MaxAmount -> token.tokenAmount - feeTotalAmount
            SendFeatureAction.ZeroAmount -> BigDecimal.ZERO
            is SendFeatureAction.NewToken,
            is SendFeatureAction.RefreshFee,
            SendFeatureAction.InitFeature -> return@flow
        }

        val newState = if (newAmount.isZero()) {
            SendState.Static.TokenZero(token, lastStaticState.fee)
        } else {
            validator.validateInputAmount(token, newAmount = newAmount, newAmountWithFee = newAmount + feeTotalAmount)
            mapper.updateInputAmount(lastStaticState, newAmount)
        }
        emit(newState)
        if (newState is SendState.Static.TokenNotZero) {
            emit(SendState.Loading.Fee(newState))
            delay(500)
        }
        transactionLoader.prepareTransaction(newState).collect {
            emit(it)
        }
    }

    private fun getFeeTotalInToken(lastStaticState: SendState.Static): BigDecimal {
        return lastStaticState.bridgeFee?.fee.getFeeList().sumOf { it.amountInToken }
    }
}
