package com.p2p.wallet.rpc.repository

import org.p2p.solanaj.model.core.PublicKey
import java.math.BigInteger

interface FeeRelayerRepository {
    suspend fun getPublicKey(): PublicKey

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