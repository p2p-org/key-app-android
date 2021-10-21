package com.p2p.wallet.auth.interactor

import android.content.SharedPreferences
import androidx.core.content.edit
import com.p2p.wallet.auth.api.CheckCaptchaResponse
import com.p2p.wallet.auth.api.CheckUsernameResponse
import com.p2p.wallet.auth.api.RegisterUsernameResponse
import com.p2p.wallet.auth.repository.UsernameRepository
import com.p2p.wallet.infrastructure.network.provider.TokenKeyProvider

private const val KEY_USERNAME = "KEY_USERNAME"

class UsernameInteractor(
    private val usernameRepository: UsernameRepository,
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
}