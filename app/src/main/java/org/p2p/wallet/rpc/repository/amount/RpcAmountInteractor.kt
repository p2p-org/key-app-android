package org.p2p.wallet.rpc.repository.amount

import org.koin.ext.getFullName
import timber.log.Timber
import java.math.BigInteger

class RpcAmountInteractor(
    private val localRepository: RpcAmountLocalRepository,
    private val remoteRepository: RpcAmountRemoteRepository
) {

    suspend fun getFees(commitment: String?): BigInteger {
        var cachedLamports = localRepository.getLamportsPerSignature()
        return if (cachedLamports != null) {
            Timber
                .tag(RpcAmountRemoteRepository::class.getFullName())
                .d("Getting from cache, lamportsPerSignature: $cachedLamports")

            cachedLamports
        } else {
            val lamports = remoteRepository.getFees(commitment)
            localRepository.setLamportsPerSignature(lamports)
            lamports
        }
    }
}