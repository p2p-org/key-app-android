package org.p2p.wallet.striga.kyc.ui

import com.google.firebase.encoders.json.BuildConfig
import com.sumsub.sns.core.data.model.SNSCompletionResult
import com.sumsub.sns.core.data.model.SNSException
import com.sumsub.sns.core.data.model.SNSSDKState
import timber.log.Timber
import java.util.Locale
import kotlinx.coroutines.launch
import org.p2p.wallet.alarmlogger.logger.AlarmErrorsLogger
import org.p2p.wallet.alarmlogger.model.StrigaAlarmError
import org.p2p.wallet.common.di.AppScope
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.striga.kyc.sdk.StrigaSdkInitParams
import org.p2p.wallet.striga.kyc.sdk.StrigaSdkListeners
import org.p2p.wallet.striga.user.interactor.StrigaUserInteractor

class StrigaKycPresenter(
    private val strigaKycInteractor: StrigaKycInteractor,
    private val strigaUserInteractor: StrigaUserInteractor,
    private val appScope: AppScope,
    private val dispatchers: CoroutineDispatchers,
    private val alarmErrorsLogger: AlarmErrorsLogger
) : BasePresenter<StrigaKycContract.View>(), StrigaKycContract.Presenter {

    private var currentKycSdkState: SNSSDKState? = null

    override fun firstAttach() {
        super.firstAttach()
        launch {
            try {
                val accessToken = fetchAccessToken().getOrThrow()
                val userEmail = strigaKycInteractor.getUserEmail()
                val userPhone = strigaKycInteractor.getUserPhone()
                val initParams = StrigaSdkInitParams(
                    initialAccessToken = accessToken,
                    accessTokenProvider = { fetchAccessToken().getOrDefault(null) },
                    userEmail = userEmail,
                    userPhoneNumber = userPhone,
                    withLogs = BuildConfig.DEBUG,
                    locale = Locale.ENGLISH,
                    StrigaSdkListeners(
                        errorListener = ::onKycError,
                        stateListener = ::onKycStateChanged,
                        completionListener = ::onKycCompletionResult,
                        actionResultListener = null,
                        eventListener = null
                    )
                )
                view?.startKyc(initParams)
            } catch (initKycError: Throwable) {
                Timber.e(initKycError, "Failed to init KYC SDK for view")
                view?.navigateBack()
            } finally {
                updateUserStatus()
            }
        }
    }

    private suspend fun fetchAccessToken(): Result<String> {
        return kotlin.runCatching { strigaKycInteractor.obtainAccessToken().unwrap() }
            .onFailure { Timber.i(it) }
    }

    private fun onKycError(error: SNSException) {
        Timber.e(error, "KYC SDK returned error")
        logAlarmError(error)
    }

    private fun onKycStateChanged(oldState: SNSSDKState, newState: SNSSDKState) {
        currentKycSdkState = newState
        Timber.d("KYC SDK state change:\n$oldState->\n$newState")
        updateUserStatus()
    }

    private fun onKycCompletionResult(result: SNSCompletionResult, state: SNSSDKState) {
        updateUserStatus()
        when (result) {
            is SNSCompletionResult.SuccessTermination -> {
                Timber.d("The SDK finished successfully, state = $state")
            }
            is SNSCompletionResult.AbnormalTermination -> {
                Timber.e(result.exception, "The SDK got closed because of errors, state = $state")
                currentKycSdkState = state
                logAlarmError(result.exception ?: Throwable("Abnormal Termination error"))
            }
        }
        view?.navigateBack()
    }

    private fun updateUserStatus() {
        appScope.launch(dispatchers.io) {
            // update user status when kyc/start called
            kotlin.runCatching { strigaUserInteractor.loadAndSaveUserStatusData().unwrap() }
                .onFailure { Timber.e(it, "Unable to load striga user status") }
        }
    }

    private fun logAlarmError(exception: Throwable) {
        val alarmError = StrigaAlarmError(
            source = "KYC SDK",
            kycSdkState = currentKycSdkState.toString(),
            error = exception
        )
        alarmErrorsLogger.triggerStrigaAlarm(alarmError)
    }
}
