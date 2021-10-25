package org.p2p.wallet.auth.repository

import org.p2p.wallet.auth.api.UsernameApi
import org.p2p.wallet.auth.api.UsernameCheckResponse
import org.p2p.wallet.auth.model.NameRegisterBody

class UsernameRemoteRepository(
    private val api: UsernameApi,
) : UsernameRepository {

    override suspend fun checkUsername(username: String): UsernameCheckResponse {
        val response = api.usernameCheck(username)
        return response
    }

    override suspend fun registerUsername(body: NameRegisterBody): String {
        val response = api.usernameRegister(body)
        return response
    }
}