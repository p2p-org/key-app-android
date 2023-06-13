package org.p2p.wallet.striga.kyc.ui

import com.google.firebase.encoders.json.BuildConfig
import com.sumsub.sns.core.data.model.SNSCompletionResult
import com.sumsub.sns.core.data.model.SNSException
import com.sumsub.sns.core.data.model.SNSSDKState
import timber.log.Timber
import java.util.Locale
import kotlinx.coroutines.launch
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.striga.kyc.sdk.StrigaSdkInitParams
import org.p2p.wallet.striga.kyc.sdk.StrigaSdkListeners

class StrigaKycPresenter(
    private val strigaKycInteractor: StrigaKycInteractor
) : BasePresenter<StrigaKycContract.View>(), StrigaKycContract.Presenter {

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
            }
        }
    }

    private suspend fun fetchAccessToken(): Result<String> {
        return kotlin.runCatching { strigaKycInteractor.obtainAccessToken().unwrap() }
            .onFailure { Timber.i(it) }
    }

    private fun onKycError(error: SNSException) {
        Timber.e(error, "KYC SDK returned error")
    }

    private fun onKycStateChanged(oldState: SNSSDKState, newState: SNSSDKState) {
        Timber.d("KYC SDK state change:\n$oldState->\n$newState")
    }

    private fun onKycCompletionResult(result: SNSCompletionResult, state: SNSSDKState) {
        when (result) {
            is SNSCompletionResult.SuccessTermination -> {
                Timber.d("The SDK finished successfully, state = $state")
            }
            is SNSCompletionResult.AbnormalTermination -> {
                Timber.e(result.exception, "The SDK got closed because of errors, state = $state")
            }
        }
        view?.navigateBack()
    }
}
