package org.p2p.wallet.auth.interactor

import androidx.core.content.edit
import android.content.SharedPreferences
import android.graphics.Bitmap
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

    @Suppress("UNUSED_PARAMETER", "RedundantSuspendModifier")
    suspend fun findUsernameByAddress(owner: String) {
        // commented due to constant problems with name service PWN-4377
//        when (val result = usernameRepository.findUsernameByAddress(owner)) {
//            is LookupResult.UsernameFound -> sharedPreferences.edit { putString(KEY_USERNAME, result.username) }
//            is LookupResult.UsernameNotFound -> Unit
//        }
    }

    fun isUsernameExists(): Boolean = sharedPreferences.contains(KEY_USERNAME)

    fun getUsername(): Username? {
        val username = sharedPreferences.getString(KEY_USERNAME, null)
        return username?.let { Username(it) }
    }

    fun saveQr(name: String, bitmap: Bitmap, forSharing: Boolean): File? = fileLocalRepository.saveQr(
        name, bitmap, forSharing
    )

    suspend fun resolveUsername(name: String): List<ResolveUsername> =
        usernameRepository.resolve(name)
}
