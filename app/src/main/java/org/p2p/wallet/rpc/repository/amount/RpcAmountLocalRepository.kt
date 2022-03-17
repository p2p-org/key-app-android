package org.p2p.wallet.rpc.repository.amount

import java.math.BigInteger

interface RpcAmountLocalRepository {
    fun setLamportsPerSignature(lamports: BigInteger)
    fun getLamportsPerSignature(): BigInteger?
    suspend fun getMinimumBalanceForRentExemption(dataLength: Int): Long?
    fun setMinimumBalanceForRentExemption(dataLength: Int, balance: Long)
}