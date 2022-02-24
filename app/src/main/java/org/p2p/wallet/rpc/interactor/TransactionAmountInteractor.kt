package org.p2p.wallet.rpc.interactor

import org.p2p.solanaj.programs.TokenProgram.AccountInfoData.ACCOUNT_INFO_DATA_LENGTH
import org.p2p.wallet.rpc.repository.RpcRepository
import java.math.BigInteger

class TransactionAmountInteractor(
    private val rpcRepository: RpcRepository
) {

    private var lamportsPerSignature: BigInteger? = null
    private var minBalanceForRentExemption: BigInteger? = null

    suspend fun getLamportsPerSignature(): BigInteger =
        lamportsPerSignature ?: rpcRepository.getFees(commitment = null).also { lamportsPerSignature = it }

    suspend fun getMinBalanceForRentExemption(dataLength: Long = ACCOUNT_INFO_DATA_LENGTH.toLong()): BigInteger =
        rpcRepository
            .getMinimumBalanceForRentExemption(dataLength)
            .toBigInteger()
            .also { minBalanceForRentExemption = it }
}