package org.p2p.wallet.auth.repository

import org.p2p.wallet.auth.api.UsernameCheckResponse
import org.p2p.wallet.auth.model.NameRegisterBody

interface UsernameRepository {
    suspend fun checkUsername(username: String): UsernameCheckResponse
    suspend fun registerUsername(body: NameRegisterBody): String
}