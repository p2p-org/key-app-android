package org.p2p.wallet.rpc.interactor

import org.p2p.solanaj.programs.TokenProgram
import org.p2p.wallet.rpc.model.NetworkFee
import org.p2p.wallet.rpc.repository.RpcRepository
import java.math.BigInteger

class TransactionAmountInteractor(
    private val rpcRepository: RpcRepository
) {

    private var feeCache: NetworkFee? = null

    suspend fun initialize() {
        if (feeCache != null) return

        val lamportsPerSignature = getLamportsPerSignature()
        val minBalanceForRentExemption = getAccountMinForRentExemption()
        feeCache = NetworkFee(lamportsPerSignature, minBalanceForRentExemption)
    }

    suspend fun getLamportsPerSignature(): BigInteger =
        feeCache?.lamportsPerSignature ?: rpcRepository.getFees(null)

    suspend fun getAccountMinForRentExemption(): BigInteger =
        feeCache?.minBalanceForRentExemption ?: rpcRepository
            .getMinimumBalanceForRentExemption(TokenProgram.AccountInfoData.ACCOUNT_INFO_DATA_LENGTH.toLong())
            .toBigInteger()
}