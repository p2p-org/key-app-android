package org.p2p.wallet.swap.repository

import org.p2p.solanaj.core.PublicKey
import org.p2p.wallet.swap.model.AccountBalance
import org.p2p.wallet.swap.model.orca.OrcaConfigs

interface OrcaSwapRepository {
    suspend fun loadTokenBalances(publicKeys: List<String>): List<Pair<String, AccountBalance>>
    suspend fun loadTokenBalance(publicKey: PublicKey): AccountBalance
    suspend fun sendAndWait(serializedTransaction: String)

    suspend fun loadOrcaConfigs(): OrcaConfigs
}
