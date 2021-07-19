package com.p2p.wallet.rpc.repository

import java.math.BigInteger

interface FeeRelayerRepository {
    suspend fun getPublicKey(): String

    suspend fun sendSolToken(
        senderPubkey: String,
        recipientPubkey: String,
        lamports: BigInteger,
        signature: String,
        blockhash: String,
    ): String

    suspend fun sendSplToken(
        senderTokenAccountPubkey: String,
        recipientPubkey: String,
        tokenMintPubkey: String,
        authorityPubkey: String,
        lamports: BigInteger,
        decimals: Int,
        signature: String,
        blockhash: String
    ): String
}