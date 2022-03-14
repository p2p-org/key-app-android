package org.p2p.wallet.rpc.interactor

import org.p2p.solanaj.programs.TokenProgram.AccountInfoData.ACCOUNT_INFO_DATA_LENGTH
import org.p2p.wallet.rpc.repository.amount.RpcAmountRepository
import java.math.BigInteger

class TransactionAmountInteractor(
    private val amountRepository: RpcAmountRepository
) {

    suspend fun getLamportsPerSignature(): BigInteger {
        val localFee = localRepository.getFees(null)
        if (localFee == null) {
            val fee = amountRepository.getFees(commitment = null)
            localRepository.updateFee(fee)
        }
        return localFee.getFee()
    }

    suspend fun getMinBalanceForRentExemption(dataLength: Int = ACCOUNT_INFO_DATA_LENGTH): BigInteger =
        amountRepository.getMinimumBalanceForRentExemption(dataLength)
}