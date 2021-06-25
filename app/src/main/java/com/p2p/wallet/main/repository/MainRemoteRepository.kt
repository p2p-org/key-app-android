package com.p2p.wallet.main.repository

import com.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import com.p2p.wallet.rpc.RpcRepository
import com.p2p.wallet.utils.toPublicKey
import org.p2p.solanaj.model.types.RecentBlockhash

class MainRemoteRepository(
    private val tokenProvider: TokenKeyProvider,
    private val rpcRepository: RpcRepository
) : MainRepository {

    override suspend fun sendToken(
        blockhash: RecentBlockhash,
        targetAddress: String,
        lamports: Long,
        tokenSymbol: String
    ): String {

        val sourcePublicKey = tokenProvider.publicKey.toPublicKey()
        val sourceSecretKey = tokenProvider.secretKey
        val targetPublicKey = targetAddress.toPublicKey()

        return rpcRepository.sendTransaction(
            sourcePublicKey,
            sourceSecretKey,
            targetPublicKey,
            lamports,
            blockhash
        )
    }
}