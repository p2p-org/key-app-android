package org.p2p.wallet.rpc.repository.amount

import java.math.BigInteger

interface RpcAmountRepository {
    suspend fun getLamportsPerSignature(commitment: String?): BigInteger
    suspend fun getMinBalanceForRentExemption(dataLength: Int): BigInteger
}
