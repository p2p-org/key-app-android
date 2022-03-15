package org.p2p.wallet.rpc.repository.balance

class RpcBalanceInMemoryRepository : RpcBalanceLocalRepository {

    private val rentExemptionCache = mutableMapOf<Int, Long>()

    override suspend fun getMinimumBalanceForRentExemption(dataLength: Int): Long? = rentExemptionCache[dataLength]

    override fun setMinimumBalanceForRentExemption(dataLength: Int, balance: Long) {
        rentExemptionCache[dataLength] = balance
    }
}