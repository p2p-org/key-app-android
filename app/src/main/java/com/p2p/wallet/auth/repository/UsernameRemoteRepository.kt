package com.p2p.wallet.auth.repository

import com.p2p.wallet.auth.api.UsernameApi
import com.p2p.wallet.auth.api.UsernameCheckResponse
import com.p2p.wallet.auth.model.NameRegisterBody

class UsernameRemoteRepository(
    private val api: UsernameApi,
) : UsernameRepository {

    override suspend fun usernameCheck(username: String): UsernameCheckResponse {
        val response = api.usernameCheck(username)
        return response
    }

    override suspend fun usernameRegister(body: NameRegisterBody): String {
        val response = api.usernameRegister(body)
        return response
    }
}