package org.p2p.wallet.renbtc.service

interface RenTransactionExecutor {
    suspend fun execute()
    fun isFinished(): Boolean
    fun getTransactionHash(): String
}
