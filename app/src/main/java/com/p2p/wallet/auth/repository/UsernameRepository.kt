package com.p2p.wallet.auth.repository

import com.p2p.wallet.auth.api.UsernameApi
import com.p2p.wallet.auth.api.UsernameCheckResponse

interface UsernameRepository {
    suspend fun usernameCheck(username: String): UsernameCheckResponse
    suspend fun usernameRegister()
}

class UsernameRemoteRepository(
    private val api: UsernameApi,
) : UsernameRepository {

    override suspend fun usernameCheck(username: String): UsernameCheckResponse {

        val response = api.usernameCheck(username)

        return response
    }

    override suspend fun usernameRegister() {
        TODO("Not yet implemented")
    }
}