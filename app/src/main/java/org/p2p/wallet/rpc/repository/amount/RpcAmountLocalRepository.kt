package org.p2p.wallet.rpc.repository.amount

import java.math.BigInteger

interface RpcAmountLocalRepository {
    fun setLamportsPerSignature(lamports: BigInteger)
    fun getLamportsPerSignature(): BigInteger?
}