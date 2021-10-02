package com.p2p.wallet.auth.repository

import com.p2p.wallet.auth.api.UsernameCheckResponse

interface UsernameRepository {
    suspend fun usernameCheck(username: String): UsernameCheckResponse
    suspend fun usernameRegister(username: String): String
}