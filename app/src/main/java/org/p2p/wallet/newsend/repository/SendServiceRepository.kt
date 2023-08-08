package org.p2p.wallet.newsend.repository

import java.math.BigInteger
import org.p2p.core.crypto.Base64String
import org.p2p.wallet.newsend.api.GenerateTransactionResponse

interface SendServiceRepository {

    suspend fun generateTransaction(
        userPublicKey: Base64String,
        tokenMint: Base64String,
        amountInLamports: BigInteger,
        recipientAddress: String,
    ): GenerateTransactionResponse
}
