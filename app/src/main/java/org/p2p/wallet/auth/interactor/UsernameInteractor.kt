package org.p2p.wallet.auth.interactor

import androidx.core.content.edit
import android.content.SharedPreferences
import android.graphics.Bitmap
import timber.log.Timber
import java.io.File
import org.p2p.wallet.auth.model.Username
import org.p2p.wallet.auth.repository.FileRepository
import org.p2p.wallet.auth.repository.UserSignUpDetailsStorage
import org.p2p.wallet.auth.username.repository.UsernameRepository
import org.p2p.wallet.auth.username.repository.model.UsernameDetails
import org.p2p.wallet.common.crashlogging.CrashLogger
import org.p2p.wallet.common.feature_toggles.toggles.remote.RegisterUsernameEnabledFeatureToggle
import org.p2p.wallet.common.feature_toggles.toggles.remote.UsernameDomainFeatureToggle
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.restore.interactor.KEY_IS_AUTH_BY_SEED_PHRASE
import org.p2p.wallet.utils.Base58String
import org.p2p.wallet.utils.toBase58Instance

const val KEY_USERNAME = "KEY_USERNAME"

class UsernameInteractor(
    private val usernameRepository: UsernameRepository,
    private val fileLocalRepository: FileRepository,
    private val tokenKeyProvider: TokenKeyProvider,
    private val userSignUpDetailsStorage: UserSignUpDetailsStorage,
    private val registerUsernameEnabledFeatureToggle: RegisterUsernameEnabledFeatureToggle,
    private val usernameDomainFeatureToggle: UsernameDomainFeatureToggle,
    private val sharedPreferences: SharedPreferences,
    private val crashLogger: CrashLogger
) {

    suspend fun isUsernameTaken(username: String): Boolean = usernameRepository.isUsernameTaken(username)

    suspend fun registerUsername(username: String) {
        usernameRepository.createUsername(
            username = username,
            ownerPublicKey = tokenKeyProvider.publicKey.toBase58Instance(),
            ownerPrivateKey = tokenKeyProvider.keyPair.toBase58Instance()
        )
        sharedPreferences.edit { putString(KEY_USERNAME, username) }
        crashLogger.setCustomKey("username", username)
    }

    suspend fun tryRestoreUsername(owner: Base58String) {
        try {
            val usernameDetails = usernameRepository.findUsernameDetailsByAddress(owner).firstOrNull()
            sharedPreferences.edit {
                if (usernameDetails != null) {
                    putString(KEY_USERNAME, usernameDetails.username.value)
                    Timber.i("Username restored for ${owner.base58Value}")
                } else {
                    // removing legacy usernames .p2p.sol
                    remove(KEY_USERNAME)
                }
            }
            crashLogger.setCustomKey("username", usernameDetails?.username?.fullUsername.orEmpty())
        } catch (error: Throwable) {
            Timber.e(error, "Failed to restore username for ${owner.base58Value}")
        }
    }

    suspend fun findUsernameByAddress(address: Base58String): UsernameDetails? {
        return usernameRepository.findUsernameDetailsByAddress(address).firstOrNull()
    }

    fun isUsernameExist(): Boolean = sharedPreferences.contains(KEY_USERNAME)

    fun getUsername(): Username? {
        val username = sharedPreferences.getString(KEY_USERNAME, null)
        return username?.let {
            Username(
                value = it,
                domainPrefix = usernameDomainFeatureToggle.value
            )
        }
    }

    fun isUsernameItemVisibleInSettings(): Boolean {
        val isUserUsedWeb3Auth = userSignUpDetailsStorage.getLastSignUpUserDetails() != null
        val isRegisterUsernameEnabled = registerUsernameEnabledFeatureToggle.isFeatureEnabled
        // sometimes user can use seed phrase to login, we cant show item to him too
        val isUsernameAuthNotBySeedPhrase = !sharedPreferences.getBoolean(KEY_IS_AUTH_BY_SEED_PHRASE, false)

        val isUsernameItemCanBeShown = getUsername() != null
        val isRegisterUsernameItemCanBeShown =
            isRegisterUsernameEnabled &&
                isUserUsedWeb3Auth &&
                isUsernameAuthNotBySeedPhrase

        // if username already exist - show it
        // if it's not, check for web3auth sign up and feature toggle
        return isUsernameItemCanBeShown || isRegisterUsernameItemCanBeShown
    }

    fun saveQr(name: String, bitmap: Bitmap, forSharing: Boolean): File? = fileLocalRepository.saveQr(
        name, bitmap, forSharing
    )
}
