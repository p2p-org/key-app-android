package org.p2p.wallet.settings.ui.security

import org.p2p.wallet.auth.interactor.AuthInteractor
import org.p2p.wallet.auth.model.BiometricStatus
import org.p2p.wallet.common.crypto.keystore.EncodeCipher
import org.p2p.wallet.common.mvp.BasePresenter
import javax.crypto.Cipher

class SecurityPresenter(private val authInteractor: AuthInteractor) :
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
        view?.showConfirmationActive(authInteractor.isOperationBiometricEnabled())
    }

    override fun onBiometricsConfirmed(cipher: Cipher) {
        authInteractor.enableFingerprintSignIn(EncodeCipher(cipher))
    }

    override fun onConfirmationStateChanged(isEnabled: Boolean) {
        authInteractor.setOperationBiometricEnable(isEnabled)
    }

    override fun setBiometricEnabled(isEnabled: Boolean) {
        if (isEnabled) {
            val cipher = authInteractor.getPinEncodeCipher()
            view?.confirmBiometrics(cipher.value)
        } else {
            authInteractor.disableBiometricSignIn()
        }
        view?.showConfirmationEnabled(isEnabled)
    }
}