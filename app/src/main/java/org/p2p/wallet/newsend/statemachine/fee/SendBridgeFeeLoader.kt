package org.p2p.wallet.newsend.statemachine.fee

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.p2p.wallet.newsend.statemachine.SendFeatureException
import org.p2p.wallet.newsend.statemachine.SendState
import org.p2p.wallet.newsend.statemachine.commonFee
import org.p2p.wallet.newsend.statemachine.commonToken
import org.p2p.wallet.newsend.statemachine.mapper.SendBridgeStaticStateMapper
import org.p2p.wallet.newsend.statemachine.model.SendFee
import org.p2p.wallet.newsend.statemachine.model.SendToken
import org.p2p.wallet.newsend.statemachine.validator.SendBridgeValidator

class SendBridgeFeeLoader constructor(
    private val mapper: SendBridgeStaticStateMapper,
    private val validator: SendBridgeValidator,
) {

    fun updateFeeIfNeed(
        lastStaticState: SendState.Static
    ): Flow<SendState> = flow {

        val token = lastStaticState.commonToken ?: return@flow
        val oldFee = lastStaticState.commonFee

        val isNeedRefresh = !validator.isFeeValid(oldFee)

        if (isNeedRefresh) {
            emit(SendState.Loading.Fee(lastStaticState))
            val fee = loadFee(token)
            emit(mapper.updateFee(lastStaticState, fee))
        }
    }

    fun updateFee(
        lastStaticState: SendState.Static
    ): Flow<SendState> = flow {

        val token = lastStaticState.commonToken ?: return@flow

        emit(SendState.Loading.Fee(lastStaticState))
        val fee = loadFee(token)
        emit(mapper.updateFee(lastStaticState, fee))
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
