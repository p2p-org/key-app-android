package org.p2p.wallet.rpc.repository.amount

import org.koin.ext.getFullName
import org.p2p.solanaj.programs.TokenProgram
import timber.log.Timber
import java.math.BigInteger

class RpcAmountInteractor(
    private val localRepository: RpcAmountLocalRepository,
    private val remoteRepository: RpcAmountRemoteRepository
) {

    suspend fun getLamportsPerSignature(commitment: String? = null): BigInteger {
        var cachedLamports = localRepository.getLamportsPerSignature()
        return if (cachedLamports != null) {
            Timber
                .tag(RpcAmountRemoteRepository::class.getFullName())
                .d("Getting from cache, lamportsPerSignature: $cachedLamports")

            cachedLamports
        } else {
            return remoteRepository.getLamportsForSignature(commitment)
        }
    }

    suspend fun getMinBalanceForRentExemption(
        dataLength: Int = TokenProgram.AccountInfoData.ACCOUNT_INFO_DATA_LENGTH,
        useCache: Boolean = true
    ): Long {
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