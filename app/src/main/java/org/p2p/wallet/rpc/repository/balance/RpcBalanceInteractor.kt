package org.p2p.wallet.rpc.repository.balance

import org.koin.ext.getFullName
import org.p2p.wallet.rpc.repository.amount.RpcAmountRepository
import timber.log.Timber

class RpcBalanceInteractor(
    private val localRepository: RpcBalanceLocalRepository,
    private val remoteRepository: RpcBalanceRemoteRepository
) {

    suspend fun getMinimumBalanceForRentExemption(dataLength: Int, useCache: Boolean = true): Long {
        var cachedBalance: Long? = null
        if (useCache) {
            cachedBalance = localRepository.getMinimumBalanceForRentExemption(dataLength)
        }
        return if (useCache && cachedBalance != null) {
            Timber
                .tag(RpcAmountRepository::class.getFullName())
                .d("Getting from cache: dataLength: $dataLength $cachedBalance")
            cachedBalance
        } else {
            val newBalance = remoteRepository.getMinimumBalanceForRentExemption(dataLength)
            localRepository.setMinimumBalanceForRentExemption(dataLength, newBalance)
            newBalance
        }
    }
}