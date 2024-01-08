package org.p2p.solanaj.rpc

import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.Transaction
import org.p2p.solanaj.model.types.AccountInfo
import org.p2p.solanaj.model.types.Encoding
import org.p2p.solanaj.model.types.SignatureInformationResponse
import org.p2p.solanaj.rpc.model.RecentPerformanceSample
import org.p2p.core.crypto.Base64String

interface RpcSolanaRepository {
    suspend fun getAccountInfo(stateKey: PublicKey): AccountInfo
    suspend fun getRecentPerformanceSamples(exampleCount: Int): List<RecentPerformanceSample>
    suspend fun sendTransaction(transaction: Transaction, signer: Account, encoding: Encoding = Encoding.BASE64): String
    suspend fun sendTransaction(serializedTransaction: String, encoding: Encoding = Encoding.BASE64): String
    suspend fun simulateTransaction(
        serializedTransaction: String,
        encoding: Encoding = Encoding.BASE64
    ): TransactionSimulationResult

    suspend fun getConfirmedSignaturesForAddress(
        mintLogAccount: PublicKey,
        limit: Int
    ): List<SignatureInformationResponse>

    suspend fun sendSerializedTransaction(serializedTransaction: Base64String, encoding: Encoding): String
}
