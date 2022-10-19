package org.p2p.wallet.auth.interactor

import androidx.core.content.edit
import android.content.SharedPreferences
import android.graphics.Bitmap
import org.p2p.wallet.auth.model.Username
import org.p2p.wallet.auth.repository.FileRepository
import org.p2p.wallet.auth.username.repository.UsernameRepository
import org.p2p.wallet.common.feature_toggles.toggles.remote.UsernameDomainFeatureToggle
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.utils.Base58String
import org.p2p.wallet.utils.toBase58Instance
import java.io.File

const val KEY_USERNAME = "KEY_USERNAME"

class UsernameInteractor(
    private val usernameRepository: UsernameRepository,
    private val fileLocalRepository: FileRepository,
    private val tokenKeyProvider: TokenKeyProvider,
    private val usernameDomainFeatureToggle: UsernameDomainFeatureToggle,
    private val sharedPreferences: SharedPreferences
) {

    suspend fun isUsernameTaken(username: String): Boolean = usernameRepository.isUsernameTaken(username)

    suspend fun registerUsername(username: String) {
        val userPublicKey = tokenKeyProvider.publicKey.toBase58Instance()
        usernameRepository.createUsername(
            username = username,
            owner = userPublicKey,
            ownerPrivateKey = tokenKeyProvider.secretKey.toBase58Instance()
        )
        sharedPreferences.edit { putString(KEY_USERNAME, username) }
    }

    @Suppress("UNUSED_PARAMETER", "RedundantSuspendModifier")
    suspend fun checkUsernameByAddress(owner: Base58String) {
        val usernameDetails = usernameRepository.findUsernameDetailsByAddress(owner).firstOrNull()
        if (usernameDetails != null) {
            sharedPreferences.edit { putString(KEY_USERNAME, usernameDetails.fullUsername) }
        }
    }

    fun isUsernameExist(): Boolean = sharedPreferences.contains(KEY_USERNAME)

    fun getUsername(): Username? {
        val username = sharedPreferences.getString(KEY_USERNAME, null)
        return username?.let {
            Username(
                trimmedUsername = it,
                domainPrefix = usernameDomainFeatureToggle.value
            )
        }
    }

    fun saveQr(name: String, bitmap: Bitmap, forSharing: Boolean): File? = fileLocalRepository.saveQr(
        name, bitmap, forSharing
    )
}
