package org.p2p.wallet.rpc.repository.balance

interface RpcBalanceLocalRepository {
    suspend fun getMinimumBalanceForRentExemption(dataLength: Int): Long?
    fun setMinimumBalanceForRentExemption(dataLength: Int, balance: Long)
}