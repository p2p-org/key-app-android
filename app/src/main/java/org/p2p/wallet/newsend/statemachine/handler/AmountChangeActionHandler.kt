package org.p2p.wallet.newsend.statemachine.handler

import java.math.BigDecimal
import kotlinx.coroutines.flow.MutableStateFlow
import org.p2p.core.utils.isZero
import org.p2p.wallet.newsend.statemachine.SendActionHandler
import org.p2p.wallet.newsend.statemachine.SendFeatureAction
import org.p2p.wallet.newsend.statemachine.SendState
import org.p2p.wallet.newsend.statemachine.bridgeToken
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

    override suspend fun handle(
        stateFlow: MutableStateFlow<SendState>,
        staticState: SendState.Static,
        newAction: SendFeatureAction
    ) {
        val token = staticState.bridgeToken ?: return
        val newAmount = when (newAction) {
            is SendFeatureAction.AmountChange -> newAction.amount
            SendFeatureAction.MaxAmount -> token.tokenAmount
            SendFeatureAction.ZeroAmount -> BigDecimal.ZERO
            is SendFeatureAction.NewToken,
            is SendFeatureAction.RefreshFee,
            SendFeatureAction.InitFeature -> return
        }

        if (newAmount.isZero()) {
            stateFlow.value = SendState.Static.TokenZero(token, staticState.fee)
        } else {
            validator.validateInputAmount(token, newAmount)
            stateFlow.value = mapper.updateInputAmount(staticState, newAmount)
            feeLoader.updateFeeIfNeed(stateFlow)
        }
    }
}
