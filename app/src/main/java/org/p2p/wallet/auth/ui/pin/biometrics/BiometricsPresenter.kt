package org.p2p.wallet.auth.ui.pin.biometrics

import kotlinx.coroutines.launch
import org.p2p.wallet.R
import org.p2p.wallet.auth.analytics.AdminAnalytics
import org.p2p.wallet.auth.analytics.OnboardingAnalytics
import org.p2p.wallet.auth.interactor.AuthInteractor
import org.p2p.wallet.auth.interactor.CreateWalletInteractor
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.crypto.keystore.EncodeCipher
import org.p2p.wallet.common.mvp.BasePresenter
import timber.log.Timber
import javax.crypto.Cipher

class BiometricsPresenter(
    private val analytics: OnboardingAnalytics,
    private val authInteractor: AuthInteractor,
    private val adminAnalytics: AdminAnalytics,
    private val createWalletInteractor: CreateWalletInteractor,
    private val analyticsInteractor: ScreensAnalyticsInteractor
) : BasePresenter<BiometricsContract.View>(), BiometricsContract.Presenter {

    override fun enableBiometric() {
        try {
            val cipher = authInteractor.getPinEncodeCipher()
            analytics.logBioApproved()
            view?.showBiometricDialog(cipher.value)
        } catch (e: Throwable) {
            Timber.e(e, "Failed to get cipher for biometrics")
            view?.showErrorMessage(R.string.error_general_message)
        }
    }

    override fun createPin(pinCode: String, cipher: Cipher?) {
        launch {
            try {
                val encoderCipher = if (cipher != null) EncodeCipher(cipher) else null
                registerComplete(pinCode, encoderCipher)
                if (cipher == null) analytics.logBioRejected()

                createWalletInteractor.finishAuthFlow()
                view?.onAuthFinished()
            } catch (e: Throwable) {
                Timber.e(e, "Failed to create pin code")
                view?.showErrorMessage(R.string.error_general_message)
            }
        }
    }

    override fun finishAuthorization() {
        analytics.logBioRejected()
        try {
            createWalletInteractor.finishAuthFlow()
            view?.onAuthFinished()
        } catch (e: Throwable) {
            Timber.e(e, "Failed to create pin code")
            view?.showErrorMessage(R.string.error_general_message)
        }
    }

    private fun registerComplete(pinCode: String, cipher: EncodeCipher?) {
        authInteractor.registerComplete(pinCode, cipher)
        // TODO determine pin complexity
        adminAnalytics.logPinCreated(currentScreenName = analyticsInteractor.getCurrentScreenName())
    }
}
