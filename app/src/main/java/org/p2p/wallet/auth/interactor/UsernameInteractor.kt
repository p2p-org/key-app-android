package org.p2p.wallet.auth.interactor

import android.content.SharedPreferences
import android.graphics.Bitmap
import androidx.core.content.edit
import org.json.JSONObject
import org.p2p.wallet.auth.model.ResolveUsername
import org.p2p.wallet.auth.model.Username
import org.p2p.wallet.auth.repository.FileRepository
import org.p2p.wallet.auth.repository.UsernameRepository
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import java.io.File

const val KEY_USERNAME = "KEY_USERNAME"

class UsernameInteractor(
    private val usernameRepository: UsernameRepository,
    private val fileLocalRepository: FileRepository,
    private val tokenKeyProvider: TokenKeyProvider,
    private val sharedPreferences: SharedPreferences
) {

    suspend fun checkUsername(username: String): String =
        usernameRepository.checkUsername(username)

    suspend fun checkCaptcha(): JSONObject =
        usernameRepository.checkCaptcha()

    suspend fun registerUsername(username: String, result: String) {
        val publicKey = tokenKeyProvider.publicKey
        usernameRepository.registerUsername(publicKey, username, result)
        sharedPreferences.edit { putString(KEY_USERNAME, username) }
    }

    suspend fun lookupUsername(owner: String) {
        val lookupUsername = usernameRepository.lookup(owner)
        sharedPreferences.edit { putString(KEY_USERNAME, lookupUsername) }
    }

    suspend fun findUsernameByAddress(owner: String): String? =
        usernameRepository.lookup(owner)

    fun usernameExists(): Boolean = sharedPreferences.contains(KEY_USERNAME)

    fun getUsername(): Username? {
        val username = sharedPreferences.getString(KEY_USERNAME, null)
        return username?.let { Username(it) }
    }

    fun saveQr(name: String, bitmap: Bitmap): File? = fileLocalRepository.saveQr(name, bitmap)

    suspend fun resolveUsername(name: String): List<ResolveUsername> =
        usernameRepository.resolve(name)
}
