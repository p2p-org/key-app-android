package org.p2p.wallet.utils

import androidx.annotation.StringRes
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.CryptoObject
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import timber.log.Timber
import javax.crypto.Cipher
import org.p2p.wallet.R

class BiometricPromptWrapper(
    fragment: Fragment,
    @StringRes private val titleRes: Int = R.string.auth_quick_access,
    @StringRes private val descriptionRes: Int = R.string.auth_biometric_question,
    @StringRes private val negativeRes: Int = R.string.common_cancel,
    private val onError: ((String?) -> Unit)? = null,
    private val onLockout: (() -> Unit)? = null,
    private val onSuccess: (Cipher) -> Unit
) {

    private val biometricCallback = object : BiometricPrompt.AuthenticationCallback() {

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            Timber.i("Error on Authentication $errorCode: $errString")

            if (errorCode == BiometricPrompt.ERROR_LOCKOUT) {
                onLockout?.invoke()
            } else {
                onError?.invoke(
                    errString.takeIf {
                        errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                            errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON
                    }
                        ?.toString()
                )
            }
        }

        override fun onAuthenticationFailed() {
            authenticateActual()
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            onSuccess(result.cryptoObject!!.cipher!!)
        }
    }

    private lateinit var cipher: Cipher

    private val biometricPrompt = BiometricPrompt(
        fragment,
        ContextCompat.getMainExecutor(fragment.requireContext()),
        biometricCallback
    )

    private val biometricPromptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle(fragment.getString(titleRes))
        .setDescription(fragment.getString(descriptionRes))
        .setNegativeButtonText(fragment.getString(negativeRes))
        .build()

    fun authenticate(cipher: Cipher) {
        this.cipher = cipher
        authenticateActual()
    }

    private fun authenticateActual() {
        biometricPrompt.authenticate(biometricPromptInfo, CryptoObject(cipher))
    }
}
