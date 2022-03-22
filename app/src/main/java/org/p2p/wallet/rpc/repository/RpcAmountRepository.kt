package org.p2p.wallet.rpc.repository

import java.math.BigInteger

interface RpcAmountRepository {
    suspend fun getFees(commitment: String?): BigInteger
    suspend fun getMinimumBalanceForRentExemption(dataLength: Int, useCache: Boolean = true): BigInteger
}
