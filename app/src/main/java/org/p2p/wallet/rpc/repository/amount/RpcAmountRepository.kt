package org.p2p.wallet.rpc.repository.amount

import java.math.BigInteger

interface RpcAmountRepository {
    suspend fun getFees(commitment: String?): BigInteger
    suspend fun getMinimumBalanceForRentExemption(dataLength: Int, useCache: Boolean = true): BigInteger
}