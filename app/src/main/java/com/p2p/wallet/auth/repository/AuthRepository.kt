package com.p2p.wallet.auth.repository

interface AuthRepository {
    suspend fun generatePhrase(): List<String>
}