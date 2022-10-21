package org.p2p.solanaj.rpc

import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.Transaction
import org.p2p.solanaj.model.types.AccountInfo
import org.p2p.solanaj.model.types.SignatureInformationResponse
import org.p2p.solanaj.rpc.model.RecentPerformanceSample

interface RpcSolanaRepository {
    suspend fun getAccountInfo(stateKey: PublicKey): AccountInfo
    suspend fun getRecentPerformanceSamples(exampleCount: Int): List<RecentPerformanceSample>
    suspend fun sendTransaction(transaction: Transaction, signer: Account): String
    suspend fun getConfirmedSignaturesForAddress(
        mintLogAccount: PublicKey,
        limit: Int
    ): List<SignatureInformationResponse>
}
