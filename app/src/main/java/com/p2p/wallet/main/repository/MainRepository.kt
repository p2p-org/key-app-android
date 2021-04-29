package com.p2p.wallet.main.repository

import org.p2p.solanaj.rpc.types.TransferInfo

interface MainRepository {
    suspend fun sendToken(targetAddress: String, lamports: Long, tokenSymbol: String): String
    suspend fun getTransaction(signature: String, slot: Long): TransferInfo?
}