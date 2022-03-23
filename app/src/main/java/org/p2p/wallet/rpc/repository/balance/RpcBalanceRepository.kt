package org.p2p.wallet.rpc.repository.balance

import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.model.types.TokenAccountBalance
import org.p2p.solanaj.model.types.TokenSupply
import java.math.BigInteger

interface RpcBalanceRepository {
    suspend fun getBalance(account: String): Long
    suspend fun getBalances(accounts: List<String>): List<Pair<String, BigInteger>>
    suspend fun getTokenAccountBalances(accounts: List<String>): List<Pair<String, TokenAccountBalance>>
    suspend fun getTokenSupply(mint: String): TokenSupply
    suspend fun getTokenAccountBalance(account: PublicKey): TokenAccountBalance
}
