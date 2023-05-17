package org.p2p.wallet.alarmlogger.model

import com.google.gson.Gson
import org.p2p.wallet.alarmlogger.api.AlarmErrorsRequest
import org.p2p.wallet.alarmlogger.api.AlarmErrorsSwapRequest
import org.p2p.wallet.infrastructure.network.data.ServerException
import org.p2p.wallet.jupiter.repository.transaction.JupiterSwapTransactionMapper
import org.p2p.wallet.jupiter.statemanager.SwapState
import org.p2p.wallet.utils.Base58String
import org.p2p.wallet.utils.toJsonObject

class AlarmSwapErrorConverter(
    private val gson: Gson,
    private val transactionMapper: JupiterSwapTransactionMapper
) {

    fun toSwapError(
        userPublicKey: Base58String,
        swapState: SwapState.SwapLoaded,
        swapError: Throwable,
        type: SwapAlarmError
    ): AlarmErrorsRequest =
        swapState.run {
            val activeRouteJson: String =
                routes[activeRoute]
                    .let { transactionMapper.toNetwork(it, userPublicKey) }
                    .route
                    .let(gson::toJsonObject)
                    .toString()

            val swapErrorStr = if (swapError is ServerException) {
                swapError.jsonErrorBody?.toString()
                    ?: swapError.message
                    ?: swapError.stackTraceToString()
            } else {
                swapError.stackTraceToString()
            }
            val request = AlarmErrorsSwapRequest(
                tokenA = AlarmErrorsSwapRequest.TokenARequest(
                    tokenName = tokenA.tokenName,
                    mint = tokenA.mintAddress,
                    amount = amountTokenA.toPlainString()
                ),
                tokenB = AlarmErrorsSwapRequest.TokenBRequest(
                    tokenName = tokenB.tokenName,
                    mint = tokenB.mintAddress,
                    amount = amountTokenB.toPlainString()
                ),
                swapRouteAsJson = activeRouteJson,
                userPublicKey = userPublicKey,
                slippage = slippage.percentValue,
                jupiterTransaction = swapState.jupiterSwapTransaction.base64Value,
                blockchainError = swapErrorStr
            )

            AlarmErrorsRequest(
                logsTitle = "Swap Android Alarm (${getSwapErrorTitle(type)})",
                payload = gson.toJsonObject(request).toString()
            )
        }

    private fun getSwapErrorTitle(type: SwapAlarmError): String = when (type) {
        SwapAlarmError.BLOCKCHAIN_ERROR -> "#blockhain"
        SwapAlarmError.LOW_SLIPPAGE -> "#low_slippage"
        SwapAlarmError.UNKNOWN -> "#unknown"
    }
}
