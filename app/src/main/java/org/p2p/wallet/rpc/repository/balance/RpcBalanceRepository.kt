package org.p2p.wallet.rpc.repository.balance

import java.math.BigInteger

interface RpcBalanceRepository {
    suspend fun getBalance(account: String): Long
    suspend fun getBalances(accounts: List<String>): List<Pair<String, BigInteger>>
}