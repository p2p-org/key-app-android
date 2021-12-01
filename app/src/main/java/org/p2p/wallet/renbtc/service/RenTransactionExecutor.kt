package org.p2p.wallet.renbtc.service

import kotlinx.coroutines.flow.MutableStateFlow
import org.p2p.wallet.renbtc.model.RenTransactionStatus

interface RenTransactionExecutor {
    suspend fun execute()
    fun getStateFlow(): MutableStateFlow<MutableList<RenTransactionStatus>>
    fun getTransactionHash(): String
}