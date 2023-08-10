package org.p2p.wallet.newsend.repository

import java.math.BigInteger
import org.p2p.core.crypto.Base58String
import org.p2p.wallet.newsend.api.GenerateSendTransactionResponse

interface SendServiceRepository {

    suspend fun generateTransaction(
        userPublicKey: Base58String,
        tokenMint: Base58String,
        amountInLamports: BigInteger,
        recipientAddress: Base58String,
    ): GenerateSendTransactionResponse
}
