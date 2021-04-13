package com.p2p.wallet.auth.interactor

import android.content.SharedPreferences
import androidx.biometric.BiometricManager
import androidx.core.content.edit
import com.p2p.wallet.auth.model.BiometricStatus
import com.p2p.wallet.auth.model.SignInResult
import com.p2p.wallet.common.crypto.HashingUtils
import com.p2p.wallet.common.crypto.Hex
import com.p2p.wallet.common.crypto.keystore.DecodeCipher
import com.p2p.wallet.common.crypto.keystore.EncodeCipher
import com.p2p.wallet.common.crypto.keystore.KeyStoreWrapper
import com.p2p.wallet.infrastructure.security.SecureStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val KEY_PIN_CODE_HASH = "KEY_PIN_CODE_HASH"
private const val KEY_PIN_CODE_ALTERNATE_HASH = "KEY_PIN_CODE_ALTERNATE_HASH"
private const val KEY_PIN_CODE_SALT = "KEY_PIN_CODE_SALT"
private const val KEY_ENABLE_FINGERPRINT_ON_SIGN_IN = "KEY_ENABLE_FINGERPRINT_ON_SIGN_IN"

class AuthInteractor(
    private val keyStoreWrapper: KeyStoreWrapper,
    private val secureStorage: SecureStorage,
    private val sharedPreferences: SharedPreferences,
    private val biometricManager: BiometricManager
) {

    // region signing in
    suspend fun signInByPinCode(pinCode: String): SignInResult = withContext(Dispatchers.Default) {
        val pinSalt = secureStorage.getBytes(KEY_PIN_CODE_SALT)
            ?: throw IllegalStateException("Pin salt does not exist")
        val pinHash = HashingUtils.generatePbkdf2Hex(pinCode, pinSalt)

        // TODO: Better to add request for backend, if pinCode is valid and remove validation locally
        val encodedHash = secureStorage.getString(KEY_PIN_CODE_ALTERNATE_HASH)
        val decodedHash = Hex.decode(encodedHash.orEmpty())
        if (pinHash == String(decodedHash)) {
            SignInResult.Success
        } else {
            SignInResult.WrongPin
        }
    }

    suspend fun signInByBiometric(cipher: DecodeCipher): SignInResult = withContext(Dispatchers.Default) {
        val pinHash = secureStorage.getString(KEY_PIN_CODE_HASH, cipher)
            ?: throw IllegalStateException("Pin hash does not exist for biometric sign in")

        if (pinHash.isEmpty()) {
            SignInResult.WrongPin
        } else {
            SignInResult.Success
        }
    }
    // endregion

    fun registerComplete(pinCode: String, cipher: EncodeCipher?) {
        val salt = HashingUtils.generateSalt()
        val hash = HashingUtils.generatePbkdf2Hex(pinCode, salt)

        if (cipher != null) {
            secureStorage.saveString(KEY_PIN_CODE_HASH, hash, cipher)
        }

        /*
         * To check if user entered valid pin code, we need to have alternative way to get hash without cipher,
         * TODO: Better to add request for backend, if pinCode is valid
         */
        secureStorage.saveString(KEY_PIN_CODE_ALTERNATE_HASH, Hex.encode(hash.toByteArray()))
        secureStorage.saveBytes(KEY_PIN_CODE_SALT, salt)
//        getAndRegisterPushToken()
    }

    fun getPinDecodeCipher(): DecodeCipher = keyStoreWrapper.getDecodeCipher(KEY_PIN_CODE_HASH)

    fun getPinEncodeCipher(): EncodeCipher = keyStoreWrapper.getEncodeCipher(KEY_PIN_CODE_HASH)

    fun getBiometricStatus(): BiometricStatus =
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                BiometricStatus.NO_HARDWARE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                BiometricStatus.NO_REGISTERED_BIOMETRIC
            else ->
                if (secureStorage.contains(KEY_PIN_CODE_HASH)) {
                    BiometricStatus.ENABLED
                } else {
                    BiometricStatus.AVAILABLE
                }
        }

    fun enableFingerprintSignIn(pinCode: String, cipher: EncodeCipher) {
        val salt = secureStorage.getBytes(KEY_PIN_CODE_SALT)
            ?: throw IllegalStateException("Pin salt does not exist")

        val hash = HashingUtils.generatePbkdf2Hex(pinCode, salt)

        secureStorage.saveString(KEY_PIN_CODE_HASH, hash, cipher)
    }

    fun disableBiometricSignIn(untilNextSignIn: Boolean = false) {
        if (untilNextSignIn) {
            sharedPreferences.edit { putBoolean(KEY_ENABLE_FINGERPRINT_ON_SIGN_IN, true) }
        }
        secureStorage.remove(KEY_PIN_CODE_HASH)
    }

    fun isAuthorized() = with(sharedPreferences) {
        contains(KEY_PIN_CODE_SALT)
    }

    fun disableBiometricSignIn() {
        secureStorage.remove(KEY_PIN_CODE_HASH)
    }

    fun logout() {
        sharedPreferences.edit { clear() }
        secureStorage.clear()
    }
}