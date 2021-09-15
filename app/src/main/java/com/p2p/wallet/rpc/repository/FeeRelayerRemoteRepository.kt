package com.p2p.wallet.rpc.repository

import com.p2p.wallet.rpc.api.FeeRelayerApi
import com.p2p.wallet.rpc.api.FeeSolTransferRequest
import com.p2p.wallet.rpc.api.FeeSplTransferRequest
import com.p2p.wallet.utils.toPublicKey
import org.p2p.solanaj.core.PublicKey
import java.math.BigInteger

class FeeRelayerRemoteRepository(
    private val api: FeeRelayerApi
) : FeeRelayerRepository {

    override suspend fun getPublicKey(): PublicKey = api.getPublicKey().toPublicKey()

    override suspend fun sendSolToken(
        senderPubkey: String,
        recipientPubkey: String,
        lamports: BigInteger,
        signature: String,
        blockhash: String
    ): String {
        val request = FeeSolTransferRequest(
            senderPubkey = senderPubkey,
            recipientPubkey = recipientPubkey,
            lamports = lamports,
            signature = signature,
            blockhash = blockhash
        )

        return api.sendSolToken(request)
    }

    override suspend fun sendSplToken(
        senderTokenAccountPubkey: String,
        recipientPubkey: String,
        tokenMintPubkey: String,
        authorityPubkey: String,
        lamports: BigInteger,
        decimals: Int,
        signature: String,
        blockhash: String
    ): String {

        val request = FeeSplTransferRequest(
            senderTokenAccountPubkey = senderTokenAccountPubkey,
            recipientPubkey = recipientPubkey,
            tokenMintPubkey = tokenMintPubkey,
            authorityPubkey = authorityPubkey,
            lamports = lamports,
            decimals = decimals,
            signature = signature,
            blockhash = blockhash
        )

        return api.sendSplToken(request)
    }
}