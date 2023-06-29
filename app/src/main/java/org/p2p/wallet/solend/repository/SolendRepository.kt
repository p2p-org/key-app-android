package org.p2p.wallet.solend.repository

import org.p2p.solanaj.core.PublicKey
import org.p2p.wallet.sdk.facade.model.solend.SolendFeePayerTokenData
import org.p2p.wallet.solend.model.SolendDepositToken
import org.p2p.wallet.solend.model.SolendTokenFee
import org.p2p.core.crypto.Base58String
import java.math.BigInteger

interface SolendRepository {
    suspend fun getUserDeposits(
        ownerAddress: Base58String,
        tokenSymbols: List<String>
    ): List<SolendDepositToken>

    suspend fun getDepositFee(
        owner: Base58String,
        feePayer: Base58String,
        tokenAmount: BigInteger,
        tokenSymbol: String
    ): SolendTokenFee

    suspend fun getWithdrawFee(
        owner: Base58String,
        feePayer: Base58String,
        tokenAmount: BigInteger,
        tokenSymbol: String
    ): SolendTokenFee

    suspend fun createWithdrawTransaction(
        relayProgramId: String,
        ownerAddress: Base58String,
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
        ownerAddress: Base58String,
        token: SolendDepositToken,
        depositAmount: BigInteger,
        remainingFreeTransactionsCount: Int,
        lendingMarketAddress: String?,
        blockhash: String,
        payFeeWithRelay: Boolean,
        feePayerToken: SolendFeePayerTokenData?,
        realFeePayerAddress: PublicKey,
    ): String
}
