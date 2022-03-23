package org.p2p.wallet.auth.repository

import org.json.JSONObject
import org.p2p.wallet.auth.api.UsernameApi
import org.p2p.wallet.auth.model.Credentials
import org.p2p.wallet.auth.model.RegisterNameRequest
import org.p2p.wallet.auth.model.ResolveUsername

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

    override suspend fun lookup(owner: String): String? {
        val response = api.lookup(owner)
        return response.firstOrNull()?.name
    }

    override suspend fun resolve(name: String): List<ResolveUsername> {
        val response = api.resolve(name)
        return response.map { ResolveUsername(it.owner, it.name) }
    }
}
