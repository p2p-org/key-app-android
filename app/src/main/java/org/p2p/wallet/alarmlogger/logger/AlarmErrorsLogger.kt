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
        if (!isLoggerEnabled || error.shouldNotBeLogged) return

        appScope.launch {
            try {
                val request = getConverter<AlarmSendErrorConverter>()
                    .toSendErrorRequest(
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
                retryRequest(
                    block = { api.sendAlarm(request) }
                )
            } catch (error: Throwable) {
                Timber.e(AlarmErrorsError(error), "Failed to send alarm")
            }
        }
    }

    fun triggerSendViaLinkAlarm(
        token: Token.Active,
        currency: String,
        lamports: BigInteger,
        error: Throwable
    ) {
        if (!isLoggerEnabled || error.shouldNotBeLogged) return

        appScope.launch {
            try {
                val request = getConverter<AlarmSendErrorConverter>()
                    .toSendViaLinkErrorRequest(
                        userPublicKey = userPublicKey,
                        token = token,
                        lamports = lamports,
                        currency = currency,
                        error = error
                    )
                retryRequest(block = { api.sendAlarm(request) })
            } catch (error: Throwable) {
                Timber.e(AlarmErrorsError(error), "Failed to send alarm")
            }
        }
    }

    fun triggerClaimViaLinkAlarm(
        token: Token.Active,
        error: Throwable
    ) {
        if (!isLoggerEnabled || error.shouldNotBeLogged) return

        appScope.launch {
            try {
                val request = getConverter<AlarmSendErrorConverter>()
                    .toClaimViaLinkErrorRequest(
                        userPublicKey = userPublicKey,
                        token = token,
                        lamports = token.totalInLamports,
                        currency = token.tokenSymbol,
                        error = error
                    )
                retryRequest(block = { api.sendAlarm(request) })
            } catch (error: Throwable) {
                Timber.e(AlarmErrorsError(error), "Failed to send alarm")
            }
        }
    }

    fun triggerSwapAlarm(
        swapErrorType: SwapAlarmError,
        swapState: SwapState.SwapLoaded,
        swapError: Throwable
    ) {
        if (!isLoggerEnabled || swapError.shouldNotBeLogged) return

        appScope.launch {
            try {
                val request = getConverter<AlarmSwapErrorConverter>()
                    .toSwapError(
                        userPublicKey = userPublicKey,
                        swapState = swapState,
                        swapError = swapError,
                        type = swapErrorType
                    )
                retryRequest(block = { api.sendAlarm(request) })
            } catch (error: Throwable) {
                Timber.e(AlarmErrorsError(error), "Failed to send alarm")
            }
        }
    }

    fun triggerBridgeClaimAlarm(
        tokenToClaim: Token.Eth,
        claimAmount: String,
        error: Throwable
    ) {
        if (!isLoggerEnabled || error.shouldNotBeLogged) return

        appScope.launch {
            try {
                val request = getConverter<AlarmBridgeErrorConverter>()
                    .toBridgeClaimErrorRequest(
                        userPublicKey = userPublicKey,
                        userEthAddress = ethInteractor.getEthUserAddress(),
                        token = tokenToClaim,
                        claimAmount = claimAmount,
                        error = error
                    )
                retryRequest(block = { api.sendAlarm(request) })
            } catch (error: Throwable) {
                Timber.e(AlarmErrorsError(error), "Failed to send alarm")
            }
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
        if (!isLoggerEnabled || error.shouldNotBeLogged) return

        appScope.launch {
            try {
                val request = getConverter<AlarmBridgeErrorConverter>()
                    .toBridgeSendErrorRequest(
                        token = token,
                        userPublicKey = userPublicKey,
                        currency = currency,
                        sendAmount = sendAmount,
                        arbiterFeeAmount = arbiterFeeAmount,
                        recipientEthPubkey = recipientEthPubkey,
                        error = error
                    )
                retryRequest(block = { api.sendAlarm(request) })
            } catch (error: Throwable) {
                Timber.e(AlarmErrorsError(error), "Failed to send alarm")
            }
        }
    }

    fun triggerUsernameAlarm(
        username: String,
        error: Throwable
    ) {
        if (!isLoggerEnabled || error.shouldNotBeLogged) return

        appScope.launch {
            try {
                val request = getConverter<AlarmSendErrorConverter>()
                    .toUsernameErrorRequest(
                        username = username,
                        userPublicKey = userPublicKey,
                        error = error
                    )
                retryRequest(block = { api.sendAlarm(request) })
            } catch (error: Throwable) {
                Timber.e(AlarmErrorsError(error), "Failed to send alarm")
            }
        }
    }

    fun triggerWeb3Alarm(web3Error: String) {
        if (!isLoggerEnabled) return

        appScope.launch {
            try {
                val request = getConverter<AlarmWeb3ErrorConverter>().toWeb3ErrorRequest(web3Error)
                retryRequest(block = { api.sendAlarm(request) })
            } catch (error: Throwable) {
                Timber.e(AlarmErrorsError(error), "Failed to send alarm")
            }
        }
    }

    fun triggerStrigaAlarm(strigaError: StrigaAlarmError) {
        if (!isLoggerEnabled || strigaError.error.shouldNotBeLogged) return

        appScope.launch {
            try {
                val request = getConverter<AlarmStrigaErrorConverter>()
                    .toStrigaErrorRequest(
                        userPublicKey = userPublicKey,
                        error = strigaError
                    )
                retryRequest(block = { api.sendAlarm(request) })
            } catch (error: Throwable) {
                Timber.e(AlarmErrorsError(error), "Failed to send alarm")
            }
        }
    }

    fun triggerDeviceShareChangeAlarm(error: DeviceShareChangeAlarmError) {
        if (!isLoggerEnabled || error.cause.shouldNotBeLogged) return

        appScope.launch {
            try {
                val request = getConverter<AlarmDeviceShareChangeErrorConverter>()
                    .toDeviceShareChangeErrorRequest(
                        userPublicKey = userPublicKey,
                        error = error
                    )
                retryRequest(block = { api.sendAlarm(request) })
            } catch (error: Throwable) {
                Timber.e(AlarmErrorsError(error), "Failed to send alarm")
            }
        }
    }

    private inline fun <reified T : AlarmFeatureConverter> getConverter(): T {
        return alarmConverters.filterIsInstance<T>().first()
    }

    private val Throwable.shouldNotBeLogged: Boolean
        get() = this is CancellationException || this.cause is CancellationException
}
