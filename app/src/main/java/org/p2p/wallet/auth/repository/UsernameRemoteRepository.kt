package org.p2p.wallet.auth.repository

import com.google.gson.Gson
import org.p2p.wallet.auth.api.CheckCaptchaResponse
import org.p2p.wallet.auth.api.UsernameApi
import org.p2p.wallet.auth.api.CheckUsernameResponse
import org.p2p.wallet.auth.api.LookupUsernameResponse
import org.p2p.wallet.auth.api.RegisterUsernameResponse
import org.p2p.wallet.auth.model.NameRegisterBody
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider

class UsernameRemoteRepository(
    private val api: UsernameApi,
    private val gson: Gson,
    private val tokenKeyProvider: TokenKeyProvider
) : UsernameRepository {

    override suspend fun checkUsername(username: String): CheckUsernameResponse {
        return api.checkUsername(username)
    }

    override suspend fun checkCaptcha(): CheckCaptchaResponse {
        return api.checkCaptcha()
    }

    override suspend fun registerUsername(username: String, result: String?): RegisterUsernameResponse {
        val credentials = gson.fromJson(result, NameRegisterBody.Credentials::class.java)
        return api.registerUsername(
            username,
            NameRegisterBody(
                owner = tokenKeyProvider.publicKey,
                credentials = credentials
            )
        )
    }

    override suspend fun lookup(owner: String): ArrayList<LookupUsernameResponse> {
        return api.lookup(owner)
    }
}