package org.p2p.wallet.solend.repository

import org.p2p.solanaj.core.PublicKey
import org.p2p.wallet.sdk.facade.model.solend.SolendFeePayerTokenData
import org.p2p.wallet.solend.model.SolendDepositFee
import org.p2p.wallet.solend.model.SolendDepositToken
import org.p2p.wallet.utils.Base58String
import java.math.BigInteger

interface SolendRepository {
    suspend fun getUserDeposits(tokenSymbols: List<String>): List<SolendDepositToken>
    suspend fun getDepositFee(owner: Base58String, tokenAmount: BigInteger, tokenSymbol: String): SolendDepositFee

    suspend fun createWithdrawTransaction(
        relayProgramId: String,
        token: SolendDepositToken,
        withdrawAmount: BigInteger,
        remainingFreeTransactionsCount: Int,
        lendingMarketAddress: String?,
        blockhash: String,
        payFeeWithRelay: Boolean,
        feePayerToken: SolendFeePayerTokenData?,
        realFeePayerAddress: PublicKey,
    ): String?

    suspend fun createDepositTransaction(
        relayProgramId: String,
        token: SolendDepositToken,
        depositAmount: BigInteger,
        remainingFreeTransactionsCount: Int,
        lendingMarketAddress: String?,
        blockhash: String,
        payFeeWithRelay: Boolean,
        feePayerToken: SolendFeePayerTokenData?,
        realFeePayerAddress: PublicKey,
    ): String?
}
