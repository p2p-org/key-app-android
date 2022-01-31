package org.p2p.wallet.rpc.interactor

import org.p2p.solanaj.programs.TokenProgram.AccountInfoData.ACCOUNT_INFO_DATA_LENGTH
import org.p2p.wallet.rpc.repository.RpcRepository
import java.math.BigInteger

class TransactionAmountInteractor(
    private val rpcRepository: RpcRepository
) {

    suspend fun getLamportsPerSignature(): BigInteger =
        rpcRepository.getFees(null)

    suspend fun getMinBalanceForRentExemption(dataLength: Long = ACCOUNT_INFO_DATA_LENGTH.toLong()): BigInteger =
        rpcRepository
            .getMinimumBalanceForRentExemption(dataLength)
            .toBigInteger()
}