package org.p2p.wallet.bridge.send.statemachine.fee

import java.math.BigDecimal
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.p2p.core.token.SolAddress
import org.p2p.core.utils.orZero
import org.p2p.core.utils.toLamports
import org.p2p.wallet.bridge.send.interactor.EthereumSendInteractor
import org.p2p.wallet.bridge.send.statemachine.SendFeatureException
import org.p2p.wallet.bridge.send.statemachine.SendState
import org.p2p.wallet.bridge.send.statemachine.bridgeToken
import org.p2p.wallet.bridge.send.statemachine.inputAmount
import org.p2p.wallet.bridge.send.statemachine.mapper.SendBridgeStaticStateMapper
import org.p2p.wallet.bridge.send.statemachine.model.SendFee
import org.p2p.wallet.bridge.send.statemachine.model.SendToken
import org.p2p.wallet.bridge.send.statemachine.validator.SendBridgeValidator

class SendBridgeFeeLoader constructor(
    private val mapper: SendBridgeStaticStateMapper,
    private val validator: SendBridgeValidator,
    private val ethereumSendInteractor: EthereumSendInteractor,
) {

    fun updateFee(
        lastStaticState: SendState.Static
    ): Flow<SendState> = flow {

        val token = lastStaticState.bridgeToken ?: return@flow

        emit(SendState.Loading.Fee(lastStaticState))
        val fee = loadFee(token, lastStaticState.inputAmount.orZero())
        emit(mapper.updateFee(lastStaticState, fee))
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

            val fee = ethereumSendInteractor.getSendFee(
                sendTokenMint = sendTokenMint,
                amount = formattedAmount.toString()
            )

            SendFee.Bridge(fee)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw SendFeatureException.FeeLoadingError(e.message)
        }
    }
}
