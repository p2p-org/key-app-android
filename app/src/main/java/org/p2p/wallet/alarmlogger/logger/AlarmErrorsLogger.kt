package org.p2p.wallet.alarmlogger.logger

import timber.log.Timber
import java.math.BigInteger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.p2p.core.common.di.AppScope
import org.p2p.core.crypto.Base58String
import org.p2p.core.crypto.toBase58Instance
import org.p2p.core.model.CurrencyMode
import org.p2p.core.token.Token
import org.p2p.wallet.alarmlogger.api.AlarmErrorsRequest
import org.p2p.wallet.alarmlogger.api.AlarmErrorsServiceApi
import org.p2p.wallet.alarmlogger.model.AlarmBridgeErrorConverter
import org.p2p.wallet.alarmlogger.model.AlarmDeviceShareChangeErrorConverter
import org.p2p.wallet.alarmlogger.model.AlarmFeatureConverter
import org.p2p.wallet.alarmlogger.model.AlarmSendErrorConverter
import org.p2p.wallet.alarmlogger.model.AlarmStrigaErrorConverter
import org.p2p.wallet.alarmlogger.model.AlarmSwapErrorConverter
import org.p2p.wallet.alarmlogger.model.AlarmWeb3ErrorConverter
import org.p2p.wallet.alarmlogger.model.DeviceShareChangeAlarmError
import org.p2p.wallet.alarmlogger.model.StrigaAlarmError
import org.p2p.wallet.alarmlogger.model.SwapAlarmError
import org.p2p.wallet.bridge.interactor.EthereumInteractor
import org.p2p.wallet.feerelayer.model.RelayAccount
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.jupiter.statemanager.SwapState
import org.p2p.wallet.newsend.model.SearchResult
import org.p2p.wallet.utils.AppBuildType
import org.p2p.wallet.utils.retryRequest

class AlarmErrorsLogger(
    private val api: AlarmErrorsServiceApi,
    private val alarmConverters: List<AlarmFeatureConverter>,
    private val tokenKeyProvider: TokenKeyProvider,
    private val ethInteractor: EthereumInteractor,
    private val appScope: AppScope
) {
    private class AlarmErrorsError(override val cause: Throwable) : Throwable(cause)

    private val userPublicKey: Base58String
        get() = tokenKeyProvider.publicKey.toBase58Instance()

    private val isLoggerEnabled: Boolean
        get() = AppBuildType.getCurrent().run { isFeatureBuild() || isReleaseBuild() }

    fun triggerSendAlarm(
        token: Token.Active,
        currencyMode: CurrencyMode,
        amount: String,
        feePayerToken: Token.Active,
        accountCreationFee: String?,
        transactionFee: String?,
        relayAccount: RelayAccount,
        recipientAddress: SearchResult,
        error: Throwable
    ) {
        launchAlarmRequest<AlarmSendErrorConverter>(error = error) {
            toSendErrorRequest(
                token = token,
                currencyMode = currencyMode,
                amount = amount,
                feePayerToken = feePayerToken,
                accountCreationFee = accountCreationFee,
                transactionFee = transactionFee,
                relayAccount = relayAccount,
                userPublicKey = userPublicKey,
                recipientAddress = recipientAddress,
                error = error
            )
        }
    }

    fun triggerSendViaLinkAlarm(
        token: Token.Active,
        currency: String,
        lamports: BigInteger,
        error: Throwable
    ) {
        launchAlarmRequest<AlarmSendErrorConverter>(error = error) {
            toSendViaLinkErrorRequest(
                userPublicKey = userPublicKey,
                token = token,
                lamports = lamports,
                currency = currency,
                error = error
            )
        }
    }

    fun triggerClaimViaLinkAlarm(token: Token.Active, error: Throwable) {
        launchAlarmRequest<AlarmSendErrorConverter>(error = error) {
            toClaimViaLinkErrorRequest(
                userPublicKey = userPublicKey,
                token = token,
                lamports = token.totalInLamports,
                currency = token.tokenSymbol,
                error = error
            )
        }
    }

    fun triggerSwapAlarm(
        swapErrorType: SwapAlarmError,
        swapState: SwapState.SwapLoaded,
        swapError: Throwable
    ) {
        launchAlarmRequest<AlarmSwapErrorConverter>(error = swapError) {
            toSwapError(
                userPublicKey = userPublicKey,
                swapState = swapState,
                swapError = swapError,
                type = swapErrorType
            )
        }
    }

    fun triggerBridgeClaimAlarm(tokenToClaim: Token.Eth, claimAmount: String, error: Throwable) {
        launchAlarmRequest<AlarmBridgeErrorConverter>(error = error) {
            toBridgeClaimErrorRequest(
                userPublicKey = userPublicKey,
                userEthAddress = ethInteractor.getEthUserAddress(),
                token = tokenToClaim,
                claimAmount = claimAmount,
                error = error
            )
        }
    }

    fun triggerBridgeSendAlarm(
        token: Token.Active,
        currency: String,
        sendAmount: String,
        arbiterFeeAmount: String,
        recipientEthPubkey: String,
        error: Throwable
    ) {
        launchAlarmRequest<AlarmBridgeErrorConverter>(error = error) {
            toBridgeSendErrorRequest(
                token = token,
                userPublicKey = userPublicKey,
                currency = currency,
                sendAmount = sendAmount,
                arbiterFeeAmount = arbiterFeeAmount,
                recipientEthPubkey = recipientEthPubkey,
                error = error
            )
        }
    }

    fun triggerUsernameAlarm(username: String, error: Throwable) {
        launchAlarmRequest<AlarmSendErrorConverter>(error = error) {
            toUsernameErrorRequest(username = username, userPublicKey = userPublicKey, error = error)
        }
    }

    fun triggerWeb3Alarm(web3Error: String) {
        launchAlarmRequest<AlarmWeb3ErrorConverter>(error = Throwable(web3Error)) {
            toWeb3ErrorRequest(web3Error)
        }
    }

    fun triggerStrigaAlarm(strigaError: StrigaAlarmError) {
        launchAlarmRequest<AlarmStrigaErrorConverter>(error = strigaError.error) {
            toStrigaErrorRequest(userPublicKey = userPublicKey, error = strigaError)
        }
    }

    fun triggerDeviceShareChangeAlarm(error: DeviceShareChangeAlarmError) {
        launchAlarmRequest<AlarmDeviceShareChangeErrorConverter>(error = error.cause) {
            toDeviceShareChangeErrorRequest(userPublicKey = userPublicKey, error = error)
        }
    }

    private inline fun <reified T : AlarmFeatureConverter> launchAlarmRequest(
        error: Throwable,
        noinline requestProvider: suspend T.() -> AlarmErrorsRequest,
    ) {
        val isErrorShouldBeLogged = error is CancellationException || error.cause is CancellationException
        if (!isLoggerEnabled || !isErrorShouldBeLogged) return

        appScope.launch {
            try {
                val requestConverter = alarmConverters.filterIsInstance<T>().first()
                val request = requestProvider.invoke(requestConverter)
                retryRequest(block = { api.sendAlarm(request) })
            } catch (requestError: Throwable) {
                Timber.i(error, "Original error to alarm")
                Timber.e(AlarmErrorsError(requestError), "Failed to send alarm")
            }
        }
    }
}
