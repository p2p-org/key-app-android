package org.p2p.wallet.rpc.interactor

import org.p2p.solanaj.programs.TokenProgram.AccountInfoData.ACCOUNT_INFO_DATA_LENGTH
import org.p2p.wallet.rpc.repository.amount.RpcAmountInteractor
import org.p2p.wallet.rpc.repository.balance.RpcBalanceInteractor
import java.math.BigInteger

class TransactionAmountInteractor(
    private val rpcBalanceInteractor: RpcBalanceInteractor,
    private val rpcAmountInteractor: RpcAmountInteractor
) {

    suspend fun getMinBalanceForRentExemption(dataLength: Int = ACCOUNT_INFO_DATA_LENGTH): BigInteger =
        rpcBalanceInteractor.getMinimumBalanceForRentExemption(dataLength).toBigInteger()

    suspend fun getLamportsPerSignature(): BigInteger =
        rpcAmountInteractor.getFees(null)
}