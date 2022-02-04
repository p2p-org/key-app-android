package org.p2p.wallet.feerelayer.repository

import org.p2p.solanaj.core.AccountMeta
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.Signature
import org.p2p.solanaj.core.TransactionInstruction
import org.p2p.wallet.feerelayer.model.SwapTransactionSignatures
import org.p2p.wallet.feerelayer.model.SwapData
import java.math.BigInteger

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
        swapData: SwapData,
        feeAmount: BigInteger,
        signatures: SwapTransactionSignatures,
        blockhash: String
    ): List<String>

    suspend fun relaySwap(
        userSourceTokenAccountPubkey: String,
        userDestinationPubkey: String,
        userDestinationAccountOwner: String?,
        sourceTokenMintPubkey: String,
        destinationTokenMintPubkey: String,
        userAuthorityPubkey: String,
        userSwap: SwapData,
        feeAmount: BigInteger,
        signatures: SwapTransactionSignatures,
        blockhash: String
    ): List<String>

    suspend fun relayTransferSplToken(
        senderTokenAccountPubkey: String,
        recipientPubkey: String,
        tokenMintPubkey: String,
        authorityPubkey: String,
        amount: BigInteger,
        decimals: Int,
        feeAmount: BigInteger,
        authoritySignature: String,
        blockhash: String,
    ): List<String>
}