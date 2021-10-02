package com.p2p.wallet.auth.interactor

import com.p2p.wallet.auth.api.UsernameCheckResponse
import com.p2p.wallet.auth.repository.UsernameRemoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReservingUsernameInteractor(
    private val usernameRemoteRepository: UsernameRemoteRepository,
) {

    suspend fun checkUsername(username: String): UsernameCheckResponse = withContext(Dispatchers.IO) {
        usernameRemoteRepository.usernameCheck(username)
    }

    suspend fun registerUsername(username: String): String = withContext(Dispatchers.IO) {
        usernameRemoteRepository.usernameRegister(username)
    }
}