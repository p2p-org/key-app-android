package org.p2p.wallet.auth.interactor

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.core.content.edit
import kotlinx.coroutines.withContext
import org.p2p.solanaj.utils.crypto.HashingUtils
import org.p2p.wallet.auth.model.BiometricStatus
import org.p2p.wallet.auth.model.BiometricType
import org.p2p.wallet.auth.model.SignInResult
import org.p2p.wallet.common.crypto.keystore.DecodeCipher
import org.p2p.wallet.common.crypto.keystore.EncodeCipher
import org.p2p.wallet.common.crypto.keystore.KeyStoreWrapper
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.infrastructure.security.SecureStorageContract

private const val KEY_PIN_CODE_BIOMETRIC_HASH = "KEY_PIN_CODE_BIOMETRIC_HASH"
private const val KEY_PIN_CODE_HASH = "KEY_PIN_CODE_HASH"
private const val KEY_PIN_CODE_SALT = "KEY_PIN_CODE_SALT"
private const val KEY_ENABLE_FINGERPRINT_ON_SIGN_IN = "KEY_ENABLE_FINGERPRINT_ON_SIGN_IN"

/**
 * The secure storage now includes the hash which is encrypted in two ways
 * If we decide to add pin code validation to the backend, we can remove one type hash
 * and make validation via pin code without decrypting hash
 * */
class AuthInteractor(
    private val keyStoreWrapper: KeyStoreWrapper,
    private val secureStorage: SecureStorageContract,
    private val sharedPreferences: SharedPreferences,
    private val biometricManager: BiometricManager,
    private val dispatchers: CoroutineDispatchers,
) {

    // region signing in
    suspend fun signInByPinCode(pinCode: String): SignInResult = withContext(dispatchers.computation) {
        val pinSalt = secureStorage.getBytes(KEY_PIN_CODE_SALT)
            ?: throw IllegalStateException("Pin salt does not exist")
        val pinHash = HashingUtils.generatePbkdf2Hex(pinCode, pinSalt)
        val currentHash = secureStorage.getString(KEY_PIN_CODE_HASH)
        if (pinHash == currentHash) {
            SignInResult.Success
        } else {
            SignInResult.WrongPin
        }
    }

    suspend fun signInByBiometric(cipher: DecodeCipher): SignInResult = withContext(dispatchers.computation) {
        val pinHash = secureStorage.getString(KEY_PIN_CODE_BIOMETRIC_HASH, cipher)
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
            secureStorage.saveString(KEY_PIN_CODE_BIOMETRIC_HASH, hash, cipher)
        }

        secureStorage.saveString(KEY_PIN_CODE_HASH, hash)
        secureStorage.saveBytes(KEY_PIN_CODE_SALT, salt)
    }

    suspend fun resetPin(pinCode: String, cipher: EncodeCipher? = null) = withContext(dispatchers.computation) {
        val salt = HashingUtils.generateSalt()
        val hash = HashingUtils.generatePbkdf2Hex(pinCode, salt)

        if (isFingerprintEnabled() && cipher != null) {
            secureStorage.saveString(KEY_PIN_CODE_BIOMETRIC_HASH, hash, cipher)
        }

        secureStorage.saveString(KEY_PIN_CODE_HASH, hash)
        secureStorage.saveBytes(KEY_PIN_CODE_SALT, salt)
    }

    fun getPinDecodeCipher(): DecodeCipher = keyStoreWrapper.getDecodeCipher(KEY_PIN_CODE_BIOMETRIC_HASH)

    fun getPinEncodeCipher(): EncodeCipher = keyStoreWrapper.getEncodeCipher(KEY_PIN_CODE_BIOMETRIC_HASH)

    fun getBiometricStatus(): BiometricStatus =
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                if (secureStorage.contains(KEY_PIN_CODE_BIOMETRIC_HASH)) {
                    BiometricStatus.ENABLED
                } else {
                    BiometricStatus.AVAILABLE
                }
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                BiometricStatus.NO_REGISTERED_BIOMETRIC

            else -> BiometricStatus.NO_HARDWARE
        }

    fun getBiometricType(context: Context): BiometricType {
        val packageManager: PackageManager = context.packageManager

        // SDK 29 adds FACE and IRIS authentication
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (packageManager.hasSystemFeature(PackageManager.FEATURE_FACE)) {
                return BiometricType.FACE_ID
            }
            if (packageManager.hasSystemFeature(PackageManager.FEATURE_IRIS)) {
                return BiometricType.IRIS
            }
            return if (packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)) {
                BiometricType.TOUCH_ID
            } else {
                BiometricType.NONE
            }
        }

        // SDK 23-28 offer FINGERPRINT only
        return if (packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)) {
            BiometricType.TOUCH_ID
        } else {
            BiometricType.NONE
        }
    }

    fun enableFingerprintSignIn(cipher: EncodeCipher) {
        val currentHash = secureStorage.getString(KEY_PIN_CODE_HASH)
            ?: throw IllegalStateException("Pin hash does not exist for sign in")

        secureStorage.saveString(KEY_PIN_CODE_BIOMETRIC_HASH, currentHash, cipher)
    }

    fun disableBiometricSignIn(untilNextSignIn: Boolean = false) {
        if (untilNextSignIn) {
            sharedPreferences.edit { putBoolean(KEY_ENABLE_FINGERPRINT_ON_SIGN_IN, true) }
        }
        secureStorage.remove(KEY_PIN_CODE_BIOMETRIC_HASH)
    }

    fun isAuthorized() = with(sharedPreferences) {
        contains(KEY_PIN_CODE_SALT)
    }

    fun disableBiometricSignIn() {
        secureStorage.remove(KEY_PIN_CODE_BIOMETRIC_HASH)
    }

    fun isFingerprintEnabled(): Boolean = getBiometricStatus() == BiometricStatus.ENABLED
}
