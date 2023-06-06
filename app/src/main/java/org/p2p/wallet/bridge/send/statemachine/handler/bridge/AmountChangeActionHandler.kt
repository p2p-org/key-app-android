package org.p2p.wallet.bridge.send.statemachine.handler.bridge

import java.math.BigDecimal
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.p2p.core.utils.isZero
import org.p2p.wallet.bridge.send.model.getFeeList
import org.p2p.wallet.bridge.send.statemachine.BridgeSendActionHandler
import org.p2p.wallet.bridge.send.statemachine.BridgeSendAction
import org.p2p.wallet.bridge.send.statemachine.BridgeSendState
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
) : BridgeSendActionHandler {

    override fun canHandle(newEvent: BridgeSendAction, staticState: BridgeSendState.Static): Boolean =
        newEvent is BridgeSendAction.AmountChange ||
            newEvent is BridgeSendAction.MaxAmount ||
            newEvent is BridgeSendAction.ZeroAmount

    override fun handle(
        currentState: BridgeSendState,
        newAction: BridgeSendAction
    ): Flow<BridgeSendState> = flow {
        val lastStaticState = currentState.lastStaticState
        val token = lastStaticState.bridgeToken ?: return@flow
        val feeTotalAmount = getFeeTotalInToken(lastStaticState)
        val newAmount = when (newAction) {
            is BridgeSendAction.AmountChange -> newAction.amount
            BridgeSendAction.MaxAmount -> token.tokenAmount - feeTotalAmount
            BridgeSendAction.ZeroAmount -> BigDecimal.ZERO
            is BridgeSendAction.NewToken,
            is BridgeSendAction.RefreshFee,
            BridgeSendAction.InitFeature -> return@flow
        }

        val newState = if (newAmount.isZero()) {
            BridgeSendState.Static.TokenZero(token, lastStaticState.fee)
        } else {
            validator.validateInputAmount(token, newAmount = newAmount, newAmountWithFee = newAmount + feeTotalAmount)
            mapper.updateInputAmount(lastStaticState, newAmount)
        }
        emit(newState)
        if (newState is BridgeSendState.Static.TokenNotZero) {
            emit(BridgeSendState.Loading.Fee(newState))
            delay(500)
        }
        transactionLoader.prepareTransaction(newState).collect {
            emit(it)
        }
    }

    private fun getFeeTotalInToken(lastStaticState: BridgeSendState.Static): BigDecimal {
        return lastStaticState.bridgeFee?.fee.getFeeList().sumOf { it.amountInToken }
    }
}
