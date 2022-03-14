package org.p2p.wallet.rpc.repository.token

import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.model.types.TokenAccountBalance
import org.p2p.solanaj.model.types.TokenSupply

interface RpcTokenRepository {

    suspend fun getTokenAccountBalances(accounts: List<String>): List<Pair<String, TokenAccountBalance>>
    suspend fun getTokenSupply(mint: String): TokenSupply
    suspend fun getTokenAccountBalance(account: PublicKey): TokenAccountBalance
}