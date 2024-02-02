package org.p2p.wallet.auth.interactor

import androidx.biometric.BiometricManager
import android.content.pm.PackageManager
import android.os.Build
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.p2p.core.common.di.AppScope
import org.p2p.core.crypto.Pbkdf2HashGenerator
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.wallet.auth.model.BiometricStatus
import org.p2p.wallet.auth.model.BiometricType
import org.p2p.wallet.auth.model.SignInResult
import org.p2p.wallet.common.crypto.keystore.DecodeCipher
import org.p2p.wallet.common.crypto.keystore.EncodeCipher
import org.p2p.wallet.home.events.AppLoaderFacade
import org.p2p.wallet.infrastructure.account.AccountStorageContract
import org.p2p.wallet.infrastructure.account.AccountStorageContract.Key
import org.p2p.wallet.infrastructure.security.SecureStorageContract
import org.p2p.wallet.infrastructure.security.SecureStorageContract.Key.KEY_PIN_CODE_BIOMETRIC_HASH
import org.p2p.wallet.infrastructure.security.SecureStorageContract.Key.KEY_PIN_CODE_HASH
import org.p2p.wallet.infrastructure.security.SecureStorageContract.Key.KEY_PIN_CODE_SALT
import org.p2p.wallet.push_notifications.interactor.PushNotificationsInteractor
import org.p2p.wallet.referral.repository.ReferralRepository

/**
 * The secure storage now includes the hash which is encrypted in two ways
 * If we decide to add pin code validation to the backend, we can remove one type hash
 * and make validation via pin code without decrypting hash
 * */
class AuthInteractor(
    private val secureStorage: SecureStorageContract,
    private val accountStorage: AccountStorageContract,
    private val biometricManager: BiometricManager,
    private val pbkdf2Hash: Pbkdf2HashGenerator,
    private val pushNotificationsInteractor: PushNotificationsInteractor,
    private val appLoaderFacade: AppLoaderFacade,
    private val dispatchers: CoroutineDispatchers,
    private val referralRepository: ReferralRepository,
    private val appScope: AppScope
) {

    // region signing in
    suspend fun signInByPinCode(enteredPinCode: String): SignInResult = withContext(dispatchers.computation) {
        val pinSalt = secureStorage.getBytes(KEY_PIN_CODE_SALT) ?: error("Pin salt does not exist")
        val pinHashFromEnteredPin = pbkdf2Hash.generateHash(
            data = enteredPinCode,
            salt = pinSalt
        )

        val currentHashInHex = secureStorage.getString(KEY_PIN_CODE_HASH)

        if (pinHashFromEnteredPin.hashResultInHex == currentHashInHex) {
            updateDeviceToken()
            SignInResult.Success
        } else {
            SignInResult.WrongPin
        }
    }

    suspend fun signInByBiometric(cipher: DecodeCipher): SignInResult = withContext(dispatchers.computation) {
        val pinHash = secureStorage.getString(KEY_PIN_CODE_BIOMETRIC_HASH, cipher)
            ?: error("Pin hash does not exist for biometric sign in")

        if (pinHash.isEmpty()) {
            SignInResult.WrongPin
        } else {
            updateDeviceToken()
            SignInResult.Success
        }
    }
    // endregion

    fun registerComplete(pinCode: String, cipher: EncodeCipher?) {
        val pinHash = pbkdf2Hash.generateHashWithRandomSalt(data = pinCode)

        if (cipher != null) {
            secureStorage.saveString(KEY_PIN_CODE_BIOMETRIC_HASH, pinHash.hashResultInHex, cipher)
        }

        secureStorage.saveString(KEY_PIN_CODE_HASH, pinHash.hashResultInHex)
        secureStorage.saveBytes(KEY_PIN_CODE_SALT, pinHash.hashSalt)
    }

    suspend fun resetPin(pinCode: String, cipher: EncodeCipher? = null) = withContext(dispatchers.computation) {
        val hashResult = pbkdf2Hash.generateHashWithRandomSalt(pinCode)

        if (isFingerprintEnabled() && cipher != null) {
            secureStorage.saveString(KEY_PIN_CODE_BIOMETRIC_HASH, hashResult.hashResultInHex, cipher)
        }

        secureStorage.saveString(KEY_PIN_CODE_HASH, hashResult.hashResultInHex)
        secureStorage.saveBytes(KEY_PIN_CODE_SALT, hashResult.hashSalt)
    }

    fun getPinDecodeCipher(): DecodeCipher = secureStorage.getDecodeCipher(KEY_PIN_CODE_BIOMETRIC_HASH)

    fun getPinEncodeCipher(): EncodeCipher = secureStorage.getEncodeCipher(KEY_PIN_CODE_BIOMETRIC_HASH)

    fun getBiometricStatus(): BiometricStatus =
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                if (secureStorage.contains(KEY_PIN_CODE_BIOMETRIC_HASH)) {
                    BiometricStatus.ENABLED
                } else {
                    BiometricStatus.AVAILABLE
                }
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                BiometricStatus.NO_REGISTERED_BIOMETRIC
            }
            else -> {
                BiometricStatus.NO_HARDWARE
            }
        }

    fun getBiometricType(packageManager: PackageManager): BiometricType {
        // SDK 29 adds FACE and IRIS authentication
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            when {
                packageManager.hasSystemFeature(PackageManager.FEATURE_FACE) -> BiometricType.FACE_ID
                packageManager.hasSystemFeature(PackageManager.FEATURE_IRIS) -> BiometricType.IRIS
                packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT) -> BiometricType.TOUCH_ID
                else -> BiometricType.NONE
            }
        } else {
            // SDK 23-28 offer FINGERPRINT only
            if (packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)) {
                BiometricType.TOUCH_ID
            } else {
                BiometricType.NONE
            }
        }
    }

    fun enableFingerprintSignIn(cipher: EncodeCipher) {
        val currentHash = secureStorage.getString(KEY_PIN_CODE_HASH)
            ?: throw IllegalStateException("Pin hash does not exist for sign in")

        secureStorage.saveString(KEY_PIN_CODE_BIOMETRIC_HASH, currentHash, cipher)
    }

    fun isAuthorized(): Boolean = secureStorage.contains(KEY_PIN_CODE_SALT)

    fun disableBiometricSignIn() {
        secureStorage.remove(KEY_PIN_CODE_BIOMETRIC_HASH)
    }

    fun isFingerprintEnabled(): Boolean = getBiometricStatus() == BiometricStatus.ENABLED

    suspend fun finishSignUp() {
        accountStorage.remove(Key.KEY_IN_SIGN_UP_PROCESS)

        updateDeviceToken()
        launchAppLoaders()
    }

    private suspend fun launchAppLoaders() = withContext(appScope.coroutineContext) {
        appLoaderFacade.load()
    }

    private fun updateDeviceToken() {
        // Send device push token to NotificationService on signIn, on creation and restoring the wallet
        appScope.launch {
            pushNotificationsInteractor.updateDeviceToken()
        }
        appScope.launch {
            referralRepository.registerReferent()
        }
    }
}
