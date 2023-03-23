package org.p2p.wallet.newsend.statemachine.handler.bridge

import java.math.BigDecimal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import org.p2p.core.utils.isZero
import org.p2p.wallet.newsend.statemachine.SendActionHandler
import org.p2p.wallet.newsend.statemachine.SendFeatureAction
import org.p2p.wallet.newsend.statemachine.SendState
import org.p2p.wallet.newsend.statemachine.commonToken
import org.p2p.wallet.newsend.statemachine.fee
import org.p2p.wallet.newsend.statemachine.fee.SendBridgeFeeLoader
import org.p2p.wallet.newsend.statemachine.mapper.SendBridgeStaticStateMapper
import org.p2p.wallet.newsend.statemachine.validator.SendBridgeValidator

class AmountChangeActionHandler(
    private val mapper: SendBridgeStaticStateMapper,
    private val validator: SendBridgeValidator,
    private val feeLoader: SendBridgeFeeLoader,
) : SendActionHandler {

    override fun canHandle(newEvent: SendFeatureAction, staticState: SendState): Boolean =
        newEvent is SendFeatureAction.AmountChange ||
            newEvent is SendFeatureAction.MaxAmount ||
            newEvent is SendFeatureAction.ZeroAmount

    override fun handle(
        lastStaticState: SendState.Static,
        newAction: SendFeatureAction
    ): Flow<SendState> = flow {
        val token = lastStaticState.commonToken ?: return@flow
        val newAmount = when (newAction) {
            is SendFeatureAction.AmountChange -> newAction.amount
            SendFeatureAction.MaxAmount -> token.tokenAmount
            SendFeatureAction.ZeroAmount -> BigDecimal.ZERO
            is SendFeatureAction.NewToken,
            is SendFeatureAction.RefreshFee,
            SendFeatureAction.InitFeature -> return@flow
        }

        if (newAmount.isZero()) {
            emit(SendState.Static.TokenZero(token, lastStaticState.fee))
        } else {
            validator.validateInputAmount(token, newAmount)
            emit(mapper.updateInputAmount(lastStaticState, newAmount))
        }
    }.flatMapMerge { feeLoader.updateFeeIfNeed(lastStaticState) }
}
