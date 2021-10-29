package org.p2p.wallet.auth.interactor

import android.content.SharedPreferences
import android.graphics.Bitmap
import androidx.core.content.edit
import org.p2p.wallet.auth.api.CheckCaptchaResponse
import org.p2p.wallet.auth.api.CheckUsernameResponse
import org.p2p.wallet.auth.api.RegisterUsernameResponse
import org.p2p.wallet.auth.model.Username
import org.p2p.wallet.auth.repository.FileRepository
import org.p2p.wallet.auth.repository.UsernameRepository
import java.io.File

private const val KEY_USERNAME = "KEY_USERNAME"

class UsernameInteractor(
    private val usernameRepository: UsernameRepository,
    private val fileLocalRepository: FileRepository,
    private val sharedPreferences: SharedPreferences
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

    suspend fun lookupUsername(owner: String) {
        val userName = usernameRepository.lookup(owner).firstOrNull()
        sharedPreferences.edit { putString(KEY_USERNAME, userName?.name) }
    }

    fun usernameExists(): Boolean = sharedPreferences.contains(KEY_USERNAME)

    fun getUsername(): Username? {
        val username = sharedPreferences.getString(KEY_USERNAME, null)
        return username?.let { Username(it) }
    }

    suspend fun saveQr(name: String, bitmap: Bitmap): File = fileLocalRepository.saveQr(name, bitmap)
}