package org.p2p.wallet.rpc.interactor

import org.p2p.solanaj.programs.TokenProgram.AccountInfoData.ACCOUNT_INFO_DATA_LENGTH
import org.p2p.wallet.rpc.repository.RpcAmountRepository
import java.math.BigInteger

class TransactionAmountInteractor(
    private val amountRepository: RpcAmountRepository
) {

    suspend fun getLamportsPerSignature(): BigInteger =
        amountRepository.getFees(commitment = null)

    suspend fun getMinBalanceForRentExemption(dataLength: Int = ACCOUNT_INFO_DATA_LENGTH): BigInteger =
        amountRepository.getMinimumBalanceForRentExemption(dataLength)
}