package org.p2p.wallet.auth.interactor

import android.content.SharedPreferences
import android.graphics.Bitmap
import androidx.core.content.edit
import org.p2p.wallet.auth.api.CheckCaptchaResponse
import org.p2p.wallet.auth.api.CheckUsernameResponse
import org.p2p.wallet.auth.api.RegisterUsernameResponse
import org.p2p.wallet.auth.repository.FileLocalRepository
import org.p2p.wallet.auth.repository.UsernameRepository
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import java.io.File

private const val KEY_USERNAME = "KEY_USERNAME"

class UsernameInteractor(
    private val usernameRepository: UsernameRepository,
    private val fileLocalRepository: FileLocalRepository,
    private val sharedPreferences: SharedPreferences,
    private val tokenKeyProvider: TokenKeyProvider
) {

    suspend fun checkUsername(username: String): CheckUsernameResponse {
        return usernameRepository.checkUsername(username)
    }

    suspend fun checkCaptcha(): CheckCaptchaResponse {
        return usernameRepository.checkCaptcha()
    }

    suspend fun registerUsername(
        username: String,
        result: String?
    ): RegisterUsernameResponse {
        return usernameRepository.registerUsername(username, result)
    }

    fun checkUsernameExist(): String? = sharedPreferences.getString(KEY_USERNAME, null)

    suspend fun lookupUsername(owner: String) {
        val userName = usernameRepository.lookup(owner).firstOrNull()
        sharedPreferences.edit { putString(KEY_USERNAME, userName?.name) }
    }

    fun getName(): String? = sharedPreferences.getString(KEY_USERNAME, null)

    fun getAddress(): String = tokenKeyProvider.publicKey

    suspend fun saveQr(name: String, bitmap: Bitmap): File = fileLocalRepository.saveQr(name, bitmap)
}