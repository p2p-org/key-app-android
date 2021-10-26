package org.p2p.wallet.swap.interactor.orca

import org.p2p.solanaj.programs.TokenProgram
import org.p2p.wallet.rpc.repository.RpcRepository
import java.math.BigInteger

class OrcaAmountInteractor(
    private val rpcRepository: RpcRepository
) {

    suspend fun getLamportsPerSignature(): BigInteger = rpcRepository.getFees(null)

    suspend fun getAccountMinForRentExemption(): BigInteger =
        rpcRepository
            .getMinimumBalanceForRentExemption(TokenProgram.AccountInfoData.ACCOUNT_INFO_DATA_LENGTH.toLong())
            .toBigInteger()
}