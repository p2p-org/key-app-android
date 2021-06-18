package com.p2p.wallet.main.repository

import com.p2p.wallet.token.model.Transaction
import org.p2p.solanaj.model.types.RecentBlockhash

interface MainRepository {
    suspend fun sendToken(
        blockhash: RecentBlockhash,
        targetAddress: String,
        lamports: Long,
        tokenSymbol: String
    ): String

    suspend fun getHistory(publicKey: String, tokenSymbol: String, limit: Int): List<Transaction>
    suspend fun getRecentBlockhash(): RecentBlockhash
}