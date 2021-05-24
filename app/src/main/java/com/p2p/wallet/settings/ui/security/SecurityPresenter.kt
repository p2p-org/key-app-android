package com.p2p.wallet.settings.ui.security

import android.content.Context
import com.p2p.wallet.auth.interactor.AuthInteractor
import com.p2p.wallet.auth.model.BiometricStatus
import com.p2p.wallet.common.crypto.keystore.EncodeCipher
import com.p2p.wallet.common.mvp.BasePresenter
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
    }

    override fun onBiometricsConfirmed(cipher: Cipher) {
        // todo: Better to prompt user to enter pin code and then enable biometrics
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