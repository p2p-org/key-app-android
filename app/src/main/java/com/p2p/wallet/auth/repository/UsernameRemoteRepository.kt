package com.p2p.wallet.auth.repository

import com.p2p.wallet.auth.api.CheckCaptchaResponse
import com.p2p.wallet.auth.api.UsernameApi
import com.p2p.wallet.auth.api.CheckUsernameResponse
import com.p2p.wallet.auth.api.RegisterUsernameResponse
import com.p2p.wallet.auth.model.NameRegisterBody

class UsernameRemoteRepository(
    private val api: UsernameApi,
) : UsernameRepository {

    override suspend fun checkUsername(username: String): CheckUsernameResponse {
        val response = api.checkUsername(username)
        return response
    }

    override suspend fun checkCaptcha(): CheckCaptchaResponse {
        val response = api.checkCaptcha()
        return response
    }

    override suspend fun registerUsername(username: String, body: NameRegisterBody): RegisterUsernameResponse {
        val response = api.registerUsername(username, body)
        return response
    }
}