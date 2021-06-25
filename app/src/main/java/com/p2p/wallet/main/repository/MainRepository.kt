package com.p2p.wallet.main.repository

import org.p2p.solanaj.model.types.RecentBlockhash

interface MainRepository {
    suspend fun sendToken(
        blockhash: RecentBlockhash,
        targetAddress: String,
        lamports: Long,
        tokenSymbol: String
    ): String
}