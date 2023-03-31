package org.p2p.wallet.feerelayer.repository

import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.Transaction
import org.p2p.wallet.feerelayer.model.FreeTransactionFeeLimit
import org.p2p.wallet.feerelayer.model.FeeRelayerStatistics
import org.p2p.wallet.feerelayer.model.SwapData
import org.p2p.wallet.feerelayer.model.SwapTransactionSignatures
import java.math.BigInteger
import org.p2p.solanaj.utils.crypto.Base64String
import org.p2p.wallet.feerelayer.model.FeeRelayerSignTransaction

interface FeeRelayerRepository {
    suspend fun getFeePayerPublicKey(): PublicKey

    suspend fun getFreeFeeLimits(owner: String): FreeTransactionFeeLimit

    suspend fun relayTransaction(transaction: Transaction, statistics: FeeRelayerStatistics): List<String>

    suspend fun signTransaction(transaction: Base64String, statistics: FeeRelayerStatistics): FeeRelayerSignTransaction

    suspend fun relayTopUpSwap(
        userSourceTokenAccountPubkey: String,
        sourceTokenMintPubkey: String,
        userAuthorityPubkey: String,
        swapData: SwapData,
        feeAmount: BigInteger,
        signatures: SwapTransactionSignatures,
        blockhash: String,
        info: FeeRelayerStatistics
    ): List<String>
}
