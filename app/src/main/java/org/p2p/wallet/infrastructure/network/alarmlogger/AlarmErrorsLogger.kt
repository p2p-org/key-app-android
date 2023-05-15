package org.p2p.wallet.infrastructure.network.alarmlogger

import com.google.gson.Gson
import timber.log.Timber
import kotlinx.coroutines.launch
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.common.di.AppScope
import org.p2p.wallet.infrastructure.network.alarmlogger.api.AlarmErrorsRequest
import org.p2p.wallet.infrastructure.network.alarmlogger.api.AlarmErrorsServiceApi
import org.p2p.wallet.infrastructure.network.alarmlogger.api.AlarmErrorsSwapRequest
import org.p2p.wallet.infrastructure.network.data.ServerException
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.jupiter.repository.transaction.JupiterSwapTransactionMapper
import org.p2p.wallet.jupiter.statemanager.SwapState
import org.p2p.wallet.utils.Base58String
import org.p2p.wallet.utils.retryRequest
import org.p2p.wallet.utils.toBase58Instance
import org.p2p.wallet.utils.toJsonObject

class AlarmErrorsLogger(
    private val api: AlarmErrorsServiceApi,
    private val tokenKeyProvider: TokenKeyProvider,
    private val transactionMapper: JupiterSwapTransactionMapper,
    private val gson: Gson,
    private val appScope: AppScope
) {
    private class AlarmErrorsError(override val cause: Throwable) : Throwable(cause)

    enum class SwapAlarmError {
        BLOCKCHAIN_ERROR, LOW_SLIPPAGE, UNKNOWN
    }

    private val userPublicKey: Base58String
        get() = tokenKeyProvider.publicKey.toBase58Instance()

    private val isReleaseBuild: Boolean
        get() = BuildConfig.BUILD_TYPE == "release"

    fun sendSwapAlarm(
        swapErrorType: SwapAlarmError,
        swapState: SwapState.SwapLoaded,
        swapError: Throwable
    ) {
        if (isReleaseBuild) {
            appScope.launch {
                try {
                    val errorTitle = getSwapErrorTitle(swapErrorType)
                    val request = gson.toJsonObject(createSwapRequest(swapState, swapError))
                        .let {
                            AlarmErrorsRequest(
                                logsTitle = "Swap Android Alarm ($errorTitle)",
                                payload = it.toString()
                            )
                        }
                    retryRequest(block = { api.sendAlarm(request) })
                } catch (error: Throwable) {
                    Timber.e(AlarmErrorsError(error), "Failed to send alarm")
                }
            }
        }
    }

    private fun getSwapErrorTitle(type: SwapAlarmError): String = when (type) {
        SwapAlarmError.BLOCKCHAIN_ERROR -> "#blockhain"
        SwapAlarmError.LOW_SLIPPAGE -> "#low_slippage"
        SwapAlarmError.UNKNOWN -> "#unknown"
    }

    private fun createSwapRequest(swapState: SwapState.SwapLoaded, swapError: Throwable) = swapState.run {
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
        AlarmErrorsSwapRequest(
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
    }
}
