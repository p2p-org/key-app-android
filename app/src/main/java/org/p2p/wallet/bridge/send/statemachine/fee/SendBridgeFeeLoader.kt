package org.p2p.wallet.bridge.send.statemachine.fee

import java.math.BigDecimal
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.p2p.core.token.SolAddress
import org.p2p.core.utils.orZero
import org.p2p.wallet.bridge.send.repository.EthereumSendRepository
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.bridge.send.statemachine.SendFeatureException
import org.p2p.wallet.bridge.send.statemachine.SendState
import org.p2p.wallet.bridge.send.statemachine.bridgeFee
import org.p2p.wallet.bridge.send.statemachine.bridgeToken
import org.p2p.wallet.bridge.send.statemachine.inputAmount
import org.p2p.wallet.bridge.send.statemachine.mapper.SendBridgeStaticStateMapper
import org.p2p.wallet.bridge.send.statemachine.model.SendFee
import org.p2p.wallet.bridge.send.statemachine.model.SendInitialData
import org.p2p.wallet.bridge.send.statemachine.model.SendToken
import org.p2p.wallet.bridge.send.statemachine.validator.SendBridgeValidator

class SendBridgeFeeLoader constructor(
    private val mapper: SendBridgeStaticStateMapper,
    private val validator: SendBridgeValidator,
    private val repository: EthereumSendRepository,
    private val tokenKeyProvider: TokenKeyProvider,
    private val initialData: SendInitialData.Bridge,
) {

    fun updateFeeIfNeed(
        lastStaticState: SendState.Static
    ): Flow<SendState> = flow {

        val token = lastStaticState.bridgeToken ?: return@flow
        val oldFee = lastStaticState.bridgeFee

        val isNeedRefresh = !validator.isFeeValid(oldFee)

        if (isNeedRefresh) {
            emit(SendState.Loading.Fee(lastStaticState))
            val fee = loadFee(token, lastStaticState.inputAmount.orZero())
            emit(mapper.updateFee(lastStaticState, fee))
        }
    }

    fun updateFee(
        lastStaticState: SendState.Static
    ): Flow<SendState> = flow {

        val token = lastStaticState.bridgeToken ?: return@flow

        emit(SendState.Loading.Fee(lastStaticState))
        val fee = loadFee(token, lastStaticState.inputAmount.orZero())
        emit(mapper.updateFee(lastStaticState, fee))
    }

    suspend fun loadFee(
        token: SendToken.Bridge,
        amount: BigDecimal,
    ): SendFee.Bridge {
        return try {
            val userWallet = SolAddress(tokenKeyProvider.publicKey)
            val mint = SolAddress(token.token.mintAddress)
            val fee = repository.getSendFee(
                userWallet = userWallet,
                recipient = initialData.recipient,
                mint = mint,
                amount.toPlainString()
            )
            SendFee.Bridge(fee)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw SendFeatureException.FeeLoadingError
        }
    }
}
