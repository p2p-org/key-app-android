package org.p2p.wallet.settings.ui.security

import android.content.Context
import org.p2p.wallet.auth.interactor.AuthInteractor
import org.p2p.wallet.auth.model.BiometricStatus
import org.p2p.wallet.common.crypto.keystore.EncodeCipher
import org.p2p.wallet.common.mvp.BasePresenter
import javax.crypto.Cipher

class SecurityPresenter(
    private val context: Context,
    private val authInteractor: AuthInteractor
) : BasePresenter<SecurityContract.View>(), SecurityContract.Presenter {

    override fun loadBiometricType() {
        val type = authInteractor.getBiometricType(context)
        view?.showBiometricData(type)

        val status = authInteractor.getBiometricStatus()
        view?.showBiometricActive(status == BiometricStatus.ENABLED)

        val notAvailableStates = listOf(
            BiometricStatus.NO_HARDWARE,
            BiometricStatus.NO_REGISTERED_BIOMETRIC
        )
        view?.showBiometricEnabled(status !in notAvailableStates)
    }

    override fun onBiometricsConfirmed(cipher: Cipher) {
        authInteractor.enableFingerprintSignIn(EncodeCipher(cipher))
    }

    override fun setBiometricEnabled(isEnabled: Boolean) {
        if (isEnabled) {
            val cipher = authInteractor.getPinEncodeCipher()
            view?.confirmBiometrics(cipher.value)
        } else {
            authInteractor.disableBiometricSignIn()
        }
    }
}