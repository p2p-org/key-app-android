package org.p2p.wallet.settings.ui.security

import javax.crypto.Cipher
import kotlinx.coroutines.launch
import org.p2p.wallet.auth.interactor.AuthInteractor
import org.p2p.wallet.auth.model.BiometricStatus
import org.p2p.wallet.common.crypto.keystore.EncodeCipher
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.settings.interactor.SettingsInteractor

class SecurityPresenter(
    private val authInteractor: AuthInteractor,
    private val settingsInteractor: SettingsInteractor
) :
    BasePresenter<SecurityContract.View>(),
    SecurityContract.Presenter {

    override fun load() {
        val status = authInteractor.getBiometricStatus()
        view?.showBiometricActive(status == BiometricStatus.ENABLED)

        val notAvailableStates = listOf(
            BiometricStatus.NO_HARDWARE,
            BiometricStatus.NO_REGISTERED_BIOMETRIC
        )
        view?.showBiometricEnabled(status !in notAvailableStates)
        view?.showConfirmationActive(settingsInteractor.isBiometricsConfirmationEnabled())
    }

    override fun onBiometricsConfirmed(cipher: Cipher) {
        authInteractor.enableFingerprintSignIn(EncodeCipher(cipher))
        view?.showBiometricActive(isActive = true)
    }

    override fun onConfirmationStateChanged(isEnabled: Boolean) {
        settingsInteractor.setBiometricsConfirmationEnabled(isEnabled)
    }

    override fun setBiometricEnabled(isEnabled: Boolean) {
        launch {
            try {
                if (isEnabled) {
                    val cipher = authInteractor.getPinEncodeCipher()
                    view?.confirmBiometrics(cipher.value)
                } else {
                    authInteractor.disableBiometricSignIn()
                }
            } catch (e: Throwable) {
                view?.showErrorMessage(e)
            }
        }
    }
}
