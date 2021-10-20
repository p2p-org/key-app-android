package com.p2p.wallet.auth.interactor

import android.content.SharedPreferences
import androidx.core.content.edit
import com.p2p.wallet.auth.api.CheckCaptchaResponse
import com.p2p.wallet.auth.api.CheckUsernameResponse
import com.p2p.wallet.auth.api.RegisterUsernameResponse
import com.p2p.wallet.auth.repository.UsernameRemoteRepository
import com.p2p.wallet.infrastructure.network.provider.TokenKeyProvider

private const val KEY_USERNAME = "KEY_USERNAME"

class UsernameInteractor(
    private val usernameRemoteRepository: UsernameRemoteRepository,
    private val sharedPreferences: SharedPreferences,
    private val tokenKeyProvider: TokenKeyProvider
) {

    suspend fun checkUsername(username: String): CheckUsernameResponse {
        return usernameRemoteRepository.checkUsername(username)
    }

    suspend fun checkCaptcha(): CheckCaptchaResponse {
        return usernameRemoteRepository.checkCaptcha()
    }

    suspend fun registerUsername(
        username: String,
        result: String?
    ): RegisterUsernameResponse {
        return usernameRemoteRepository.registerUsername(username, result)
    }

    fun checkUsernameExist(): String? {
        return if (sharedPreferences.contains(KEY_USERNAME))
            sharedPreferences.getString(KEY_USERNAME, null)
        else
            ""
    }

    suspend fun lookupUsername(owner: String) {
        val userName = usernameRemoteRepository.lookup(owner).firstOrNull()
        sharedPreferences.edit { putString(KEY_USERNAME, userName?.name) }
//        sharedPreferences.edit { putString(KEY_USERNAME, "konstantink.p2p.sol") }
    }

    fun getName(): String? = sharedPreferences.getString(KEY_USERNAME, null)
    fun getAddress(): String = tokenKeyProvider.publicKey
}