package com.p2p.wallet.auth.interactor

import com.p2p.wallet.auth.api.CheckCaptchaResponse
import com.p2p.wallet.auth.api.CheckUsernameResponse
import com.p2p.wallet.auth.api.RegisterUsernameResponse
import com.p2p.wallet.auth.repository.UsernameRemoteRepository

class ReservingUsernameInteractor(
    private val usernameRemoteRepository: UsernameRemoteRepository,
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
}