package com.p2p.wallet.main.repository

import com.p2p.wallet.token.model.Transaction

interface MainRepository {
    suspend fun sendToken(targetAddress: String, lamports: Long, tokenSymbol: String): String
    suspend fun getHistory(depositAddress: String, tokenSymbol: String, limit: Int): List<Transaction>
}