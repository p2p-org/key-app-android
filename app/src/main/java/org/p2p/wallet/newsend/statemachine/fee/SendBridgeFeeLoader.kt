package org.p2p.wallet.newsend.statemachine.fee

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import org.p2p.wallet.newsend.statemachine.SendFeatureException
import org.p2p.wallet.newsend.statemachine.SendState
import org.p2p.wallet.newsend.statemachine.commonFee
import org.p2p.wallet.newsend.statemachine.commonToken
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
        val token = staticState.commonToken ?: return
        val oldFee = staticState.commonFee

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
        val token = staticState.commonToken ?: return

        stateFlow.value = SendState.Loading.Fee(staticState)
        val fee = loadFee(token)
        stateFlow.value = mapper.updateFee(staticState, fee)
    }

    suspend fun loadFee(token: SendToken.Common): SendFee.Common {
        return try {
            // todo loading
            delay(2000)
            SendFee.mockCommon()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw SendFeatureException.FeeLoadingError
        }
    }
}
