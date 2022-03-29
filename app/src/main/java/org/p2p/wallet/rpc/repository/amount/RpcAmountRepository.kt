package org.p2p.wallet.rpc.repository.amount

import org.p2p.solanaj.programs.TokenProgram.AccountInfoData.ACCOUNT_INFO_DATA_LENGTH
import java.math.BigInteger

interface RpcAmountRepository {
    suspend fun getLamportsPerSignature(commitment: String? = null): BigInteger
    suspend fun getMinBalanceForRentExemption(dataLength: Int = ACCOUNT_INFO_DATA_LENGTH): BigInteger
}
