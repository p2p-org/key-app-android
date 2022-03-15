package org.p2p.wallet.rpc.repository.amount

import java.math.BigInteger

class RpcAmountInMemoryRepository : RpcAmountLocalRepository {

    private var lamportsPerSignature: BigInteger? = null

    override fun setLamportsPerSignature(lamports: BigInteger) {
        lamportsPerSignature = lamports
    }

    override fun getLamportsPerSignature(): BigInteger? = lamportsPerSignature
}