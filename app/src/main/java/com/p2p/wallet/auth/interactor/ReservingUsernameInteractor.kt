package com.p2p.wallet.auth.interactor

import com.p2p.wallet.auth.api.CheckCaptchaResponse
import com.p2p.wallet.auth.api.CheckUsernameResponse
import com.p2p.wallet.auth.api.RegisterUsernameResponse
import com.p2p.wallet.auth.model.NameRegisterBody
import com.p2p.wallet.auth.repository.UsernameRemoteRepository
import com.p2p.wallet.infrastructure.network.provider.TokenKeyProvider

class ReservingUsernameInteractor(
    private val usernameRemoteRepository: UsernameRemoteRepository,
    private val tokenKeyProvider: TokenKeyProvider
) {

    suspend fun checkUsername(username: String): CheckUsernameResponse {
        return usernameRemoteRepository.checkUsername(username)
    }

    suspend fun checkCaptcha(): CheckCaptchaResponse {
        return usernameRemoteRepository.checkCaptcha()
    }

    suspend fun registerUsername(username: String, credentials: NameRegisterBody.Credentials): RegisterUsernameResponse {
        return usernameRemoteRepository.registerUsername(
            username,
            NameRegisterBody(
                owner = tokenKeyProvider.publicKey,
                credentials = credentials
            )
        )
    }
}