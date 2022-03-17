package org.p2p.wallet.rpc.repository.amount

import java.math.BigInteger

class RpcAmountInMemoryRepository : RpcAmountLocalRepository {

    private var lamportsPerSignature: BigInteger? = null
    private val rentExemptionCache = mutableMapOf<Int, Long>()

    override fun setLamportsPerSignature(lamports: BigInteger) {
        lamportsPerSignature = lamports
    }

    override fun getLamportsPerSignature(): BigInteger? = lamportsPerSignature

    override suspend fun getMinimumBalanceForRentExemption(dataLength: Int): Long? = rentExemptionCache[dataLength]

    override fun setMinimumBalanceForRentExemption(dataLength: Int, balance: Long) {
        rentExemptionCache[dataLength] = balance
    }
}