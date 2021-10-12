package com.p2p.wallet.auth.interactor

import com.p2p.wallet.auth.api.GetCaptchaResponse
import com.p2p.wallet.auth.api.UsernameCheckResponse
import com.p2p.wallet.auth.model.NameRegisterBody
import com.p2p.wallet.auth.repository.UsernameRemoteRepository

class ReservingUsernameInteractor(
    private val usernameRemoteRepository: UsernameRemoteRepository,
) {

    suspend fun checkUsername(username: String): UsernameCheckResponse {
        return usernameRemoteRepository.checkUsername(username)
    }

    suspend fun checkCaptcha(): GetCaptchaResponse {
        return usernameRemoteRepository.checkCaptcha()
    }

    suspend fun registerUsername(nameRegisterBody: NameRegisterBody): String {
        return usernameRemoteRepository.registerUsername(nameRegisterBody)
    }
}