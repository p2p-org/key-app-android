package com.p2p.wallet.auth.repository

import com.p2p.wallet.auth.api.UsernameApi
import com.p2p.wallet.auth.api.UsernameCheckResponse

class UsernameRemoteRepository(
    private val api: UsernameApi,
) : UsernameRepository {

    override suspend fun usernameCheck(username: String): UsernameCheckResponse {
//        val response = api.usernameCheck(username)
        val response = api.usernameCheck("kstep-test-1")

        return response
    }

    override suspend fun usernameRegister(username: String): String {
        val response = api.usernameRegister(username)
        return response
    }
}