package org.p2p.wallet.auth.repository

import org.json.JSONObject
import org.p2p.wallet.auth.model.LookupResult
import org.p2p.wallet.auth.model.ResolveUsername

interface UsernameRepository {
    suspend fun checkUsername(username: String): String
    suspend fun checkCaptcha(): JSONObject
    suspend fun registerUsername(publicKey: String, username: String, result: String)
    suspend fun findUsernameByAddress(owner: String): LookupResult
    suspend fun resolve(name: String): List<ResolveUsername>
}
