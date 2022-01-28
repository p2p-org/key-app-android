package org.p2p.wallet.feerelayer.repository

import org.p2p.solanaj.core.AccountMeta
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.Signature
import org.p2p.solanaj.core.TransactionInstruction
import org.p2p.wallet.feerelayer.model.SwapTransactionSignatures
import org.p2p.wallet.feerelayer.model.TopUpSwap

interface FeeRelayerRepository {
    suspend fun getFeePayerPublicKey(): PublicKey

    suspend fun relayTransaction(
        instructions: List<TransactionInstruction>,
        signatures: List<Signature>,
        pubkeys: List<AccountMeta>,
        blockHash: String
    ): List<String>

    suspend fun relayTopUpSwap(
        userSourceTokenAccountPubkey: String,
        sourceTokenMintPubkey: String,
        userAuthorityPubkey: String,
        topUpSwap: TopUpSwap,
        feeAmount: Long,
        signatures: SwapTransactionSignatures,
        blockhash: String
    ): List<String>
}