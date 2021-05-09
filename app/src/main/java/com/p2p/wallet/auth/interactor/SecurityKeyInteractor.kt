package com.p2p.wallet.auth.interactor

import com.p2p.wallet.auth.repository.AuthRepository

class SecurityKeyInteractor(
    private val authRepository: AuthRepository
) {

    suspend fun generateKeys(): List<String> =
        authRepository.generatePhrase()
}