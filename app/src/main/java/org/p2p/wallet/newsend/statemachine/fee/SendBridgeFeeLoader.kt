package org.p2p.wallet.newsend.statemachine.fee

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import org.p2p.wallet.newsend.statemachine.SendFeatureException
import org.p2p.wallet.newsend.statemachine.SendState
import org.p2p.wallet.newsend.statemachine.bridgeFee
import org.p2p.wallet.newsend.statemachine.bridgeToken
import org.p2p.wallet.newsend.statemachine.lastStaticState
import org.p2p.wallet.newsend.statemachine.mapper.SendBridgeStaticStateMapper
import org.p2p.wallet.newsend.statemachine.model.SendFee
import org.p2p.wallet.newsend.statemachine.model.SendToken
import org.p2p.wallet.newsend.statemachine.validator.SendBridgeValidator

class SendBridgeFeeLoader constructor(
    private val mapper: SendBridgeStaticStateMapper,
    private val validator: SendBridgeValidator,
) {

    suspend fun updateFeeIfNeed(
        stateFlow: MutableStateFlow<SendState>,
    ) {

        val staticState = stateFlow.lastStaticState
        val token = staticState.bridgeToken ?: return
        val oldFee = staticState.bridgeFee

        val isNeedRefresh = !validator.isFeeValid(oldFee)

        if (isNeedRefresh) {
            stateFlow.value = SendState.Loading.Fee(staticState)
            val fee = loadFee(token)
            stateFlow.value = mapper.updateFee(staticState, fee)
        }
    }

    suspend fun updateFee(
        stateFlow: MutableStateFlow<SendState>,
    ) {

        val staticState = stateFlow.lastStaticState
        val token = staticState.bridgeToken ?: return

        stateFlow.value = SendState.Loading.Fee(staticState)
        val fee = loadFee(token)
        stateFlow.value = mapper.updateFee(staticState, fee)
    }

    suspend fun loadFee(token: SendToken.Bridge): SendFee.Bridge {
        return try {
            // todo loading
            delay(2000)
            SendFee.mockBridge()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw SendFeatureException.FeeLoadingError
        }
    }
}
