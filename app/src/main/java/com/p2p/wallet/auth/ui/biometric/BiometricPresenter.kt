package com.p2p.wallet.auth.ui.biometric

import com.p2p.wallet.R
import com.p2p.wallet.auth.interactor.AuthInteractor
import com.p2p.wallet.common.crypto.keystore.EncodeCipher
import com.p2p.wallet.common.mvp.BasePresenter
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.crypto.Cipher

class BiometricPresenter(
    private val authInteractor: AuthInteractor
) : BasePresenter<BiometricContract.View>(), BiometricContract.Presenter {

    override fun createPin(pinCode: String, cipher: Cipher?) {
        launch {
            try {
                val encoderCipher = if (cipher != null) EncodeCipher(cipher) else null
                authInteractor.registerComplete(pinCode, encoderCipher)
                view?.onAuthFinished()
            } catch (e: Throwable) {
                Timber.e(e, "Failed to create pin code")
                view?.showErrorMessage(R.string.error_general_message)
            }
        }
    }

    override fun enableBiometric() {
        try {
            val cipher = authInteractor.getPinEncodeCipher()
            view?.showBiometricDialog(cipher.value)
        } catch (e: Throwable) {
            Timber.e(e, "Failed to get cipher for biometrics")
            view?.showErrorMessage(R.string.error_general_message)
        }
    }
}