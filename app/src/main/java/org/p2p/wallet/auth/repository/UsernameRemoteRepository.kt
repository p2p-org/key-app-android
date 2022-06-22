package org.p2p.wallet.auth.repository

import org.json.JSONObject
import org.p2p.wallet.auth.api.UsernameApi
import org.p2p.wallet.auth.model.Credentials
import org.p2p.wallet.auth.model.LookupResult
import org.p2p.wallet.auth.model.RegisterNameRequest
import org.p2p.wallet.auth.model.ResolveUsername
import org.p2p.wallet.infrastructure.network.data.ErrorCode
import org.p2p.wallet.infrastructure.network.data.ServerException

class UsernameRemoteRepository(
    private val api: UsernameApi
) : UsernameRepository {

    override suspend fun checkUsername(username: String): String =
        api.checkUsername(username).owner

    override suspend fun checkCaptcha(): JSONObject {
        val captcha = api.checkCaptcha().toString()
        return JSONObject(captcha)
    }

    override suspend fun registerUsername(publicKey: String, username: String, result: String) {
        val jsonObject = JSONObject(result)
        val geeTestValidate = jsonObject.optString("geetest_validate")
        val geeTestSecCode = jsonObject.optString("geetest_seccode")
        val geeTestChallenge = jsonObject.optString("geetest_challenge")
        val credentials = Credentials(geeTestValidate, geeTestSecCode, geeTestChallenge)
        val body = RegisterNameRequest(publicKey, credentials)
        api.registerUsername(username, body)
    }

    override suspend fun findUsernameByAddress(owner: String): LookupResult =
        try {
            val username = api.lookup(owner).firstOrNull()
            if (username != null) {
                LookupResult.UsernameFound(username.name)
            } else {
                LookupResult.UsernameNotFound
            }
        } catch (e: ServerException) {
            if (e.errorCode == ErrorCode.SERVER_ERROR) {
                LookupResult.UsernameNotFound
            } else {
                throw e
            }
        }

    override suspend fun resolve(name: String): List<ResolveUsername> {
        val response = api.resolve(name)
        return response.map { ResolveUsername(it.owner, it.name) }
    }
}
