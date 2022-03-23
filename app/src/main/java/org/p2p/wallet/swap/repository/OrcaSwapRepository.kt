package org.p2p.wallet.swap.repository

import org.p2p.solanaj.core.PublicKey
import org.p2p.wallet.swap.model.AccountBalance

interface OrcaSwapRepository {
    suspend fun loadTokenBalances(publicKeys: List<String>): List<Pair<String, AccountBalance>>
    suspend fun loadTokenBalance(publicKey: PublicKey): AccountBalance
    suspend fun sendAndWait(serializedTransaction: String)
}
