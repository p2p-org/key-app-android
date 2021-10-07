package com.p2p.wallet.auth.repository

import com.p2p.wallet.auth.api.GetCaptchaResponse
import com.p2p.wallet.auth.api.UsernameApi
import com.p2p.wallet.auth.api.UsernameCheckResponse
import com.p2p.wallet.auth.model.NameRegisterBody

class UsernameRemoteRepository(
    private val api: UsernameApi,
) : UsernameRepository {

    override suspend fun checkUsername(username: String): UsernameCheckResponse {
        val response = api.checkUsername(username)
        return response
    }

    override suspend fun checkCaptcha(): GetCaptchaResponse {
        val response = api.checkCaptcha()
        return response
    }

    override suspend fun registerUsername(body: NameRegisterBody): String {
        val response = api.registerUsername(body)
        return response
    }
}