package org.p2p.wallet.auth.repository

import org.json.JSONObject
import org.p2p.wallet.auth.model.CheckUsername
import org.p2p.wallet.auth.model.LookupUsername
import org.p2p.wallet.auth.model.RegisterUsername
import org.p2p.wallet.auth.model.ResolveUsername

interface UsernameRepository {
    suspend fun checkUsername(username: String): CheckUsername
    suspend fun checkCaptcha(): JSONObject
    suspend fun registerUsername(username: String, result: String?): RegisterUsername
    suspend fun lookup(owner: String): LookupUsername
    suspend fun resolve(name: String): List<ResolveUsername>
}