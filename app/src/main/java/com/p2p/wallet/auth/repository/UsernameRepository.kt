package com.p2p.wallet.auth.repository

import com.p2p.wallet.auth.api.UsernameCheckResponse
import com.p2p.wallet.auth.model.NameRegisterBody

interface UsernameRepository {
    suspend fun usernameCheck(username: String): UsernameCheckResponse
    suspend fun usernameRegister(body: NameRegisterBody): String
}