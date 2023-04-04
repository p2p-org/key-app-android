package org.p2p.wallet.bridge.send.statemachine.handler.bridge

import java.math.BigDecimal
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.p2p.core.utils.isZero
import org.p2p.wallet.bridge.send.statemachine.SendActionHandler
import org.p2p.wallet.bridge.send.statemachine.SendFeatureAction
import org.p2p.wallet.bridge.send.statemachine.SendState
import org.p2p.wallet.bridge.send.statemachine.bridgeToken
import org.p2p.wallet.bridge.send.statemachine.fee
import org.p2p.wallet.bridge.send.statemachine.fee.SendBridgeFeeLoader
import org.p2p.wallet.bridge.send.statemachine.mapper.SendBridgeStaticStateMapper
import org.p2p.wallet.bridge.send.statemachine.validator.SendBridgeValidator

class AmountChangeActionHandler(
    private val mapper: SendBridgeStaticStateMapper,
    private val validator: SendBridgeValidator,
    private val feeLoader: SendBridgeFeeLoader,
) : SendActionHandler {

    override fun canHandle(newEvent: SendFeatureAction, staticState: SendState.Static): Boolean =
        newEvent is SendFeatureAction.AmountChange ||
            newEvent is SendFeatureAction.MaxAmount ||
            newEvent is SendFeatureAction.ZeroAmount

    override fun handle(
        lastStaticState: SendState.Static,
        newAction: SendFeatureAction
    ): Flow<SendState> = flow {
        val token = lastStaticState.bridgeToken ?: return@flow
        val newAmount = when (newAction) {
            is SendFeatureAction.AmountChange -> newAction.amount
            SendFeatureAction.MaxAmount -> token.tokenAmount
            SendFeatureAction.ZeroAmount -> BigDecimal.ZERO
            is SendFeatureAction.NewToken,
            is SendFeatureAction.RefreshFee,
            SendFeatureAction.InitFeature -> return@flow
        }

        val newState = if (newAmount.isZero()) {
            SendState.Static.TokenZero(token, lastStaticState.fee)
        } else {
            validator.validateInputAmount(token, newAmount)
            mapper.updateInputAmount(lastStaticState, newAmount)
        }
        emit(newState)
        if (newState is SendState.Static.TokenNotZero) {
            emit(SendState.Loading.Fee(newState))
            delay(500)
        }
        feeLoader.updateFee(newState).collect {
            emit(it)
        }
    }
}
