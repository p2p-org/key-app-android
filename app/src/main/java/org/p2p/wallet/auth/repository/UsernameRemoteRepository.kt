package org.p2p.wallet.auth.repository

import com.google.gson.Gson
import org.json.JSONObject
import org.p2p.wallet.auth.api.UsernameApi
import org.p2p.wallet.auth.model.CheckUsername
import org.p2p.wallet.auth.model.LookupUsername
import org.p2p.wallet.auth.model.NameRegisterBody
import org.p2p.wallet.auth.model.RegisterUsername
import org.p2p.wallet.auth.model.ResolveUsername
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider

class UsernameRemoteRepository(
    private val api: UsernameApi,
    private val gson: Gson,
    private val tokenKeyProvider: TokenKeyProvider
) : UsernameRepository {

    override suspend fun checkUsername(username: String): CheckUsername {
        val response = api.checkUsername(username)
        return CheckUsername(
            owner = response.owner
        )
    }

    override suspend fun checkCaptcha(): JSONObject {
        return api.checkCaptcha()
    }

    override suspend fun registerUsername(username: String, result: String?): RegisterUsername {
        val credentials = gson.fromJson(result, NameRegisterBody.Credentials::class.java)
        val response = api.registerUsername(
            username,
            NameRegisterBody(
                owner = tokenKeyProvider.publicKey,
                credentials = credentials
            )
        )
        return RegisterUsername(
            signature = response.signature
        )
    }

    override suspend fun lookup(owner: String): LookupUsername {
        val response = api.lookup(owner)
        return LookupUsername(
            name = response.firstOrNull()?.name
        )
    }

    override suspend fun resolve(name: String): List<ResolveUsername> {
        val response = api.resolve(name)
        return response.withIndex().flatMap { (_, transaction) ->
            listOf(
                ResolveUsername(
                    owner = transaction.owner,
                    name = transaction.name,
                )
            )
        }
    }
}