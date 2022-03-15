package org.p2p.wallet.rpc.repository.amount

import java.math.BigInteger

interface RpcAmountRepository {
    suspend fun getFees(commitment: String?): BigInteger
}