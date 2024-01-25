package org.p2p.wallet.send.interactor.usecase

import timber.log.Timber
import java.math.BigInteger
import org.p2p.core.crypto.Base58String
import org.p2p.core.crypto.toBase58Instance
import org.p2p.core.token.Token
import org.p2p.core.utils.Constants
import org.p2p.solanaj.core.FeeAmount
import org.p2p.token.service.converter.TokenServiceAmountsConverter
import org.p2p.wallet.send.repository.SendServiceRepository

class GetFeesInPayingTokenUseCase(
    private val amountsConverter: TokenServiceAmountsConverter,
    private val sendServiceRepository: SendServiceRepository
) {

    suspend fun execute(
        findFeesIn: List<Base58String>,
        transactionFeeInSol: BigInteger,
        accountCreationFeeInSol: BigInteger
    ): Map<Base58String, BigInteger> = try {
        val totalFees = accountCreationFeeInSol
        val feesInSpl = amountsConverter.convertAmount(
            amountFrom = Constants.WRAPPED_SOL_MINT.toBase58Instance() to totalFees,
            mintsToConvertTo = findFeesIn
        )

        if (feesInSpl.isNotEmpty()) {
            Timber.i("-> Fees found: $feesInSpl")
            feesInSpl
        } else {
            Timber.i("-> Fees are null found for $findFeesIn")
            emptyMap()
        }
    } catch (e: IllegalStateException) {
        Timber.i(e, "-> No fees found, error")
        // ignoring tokens without fees
        emptyMap()
    }

    suspend fun execute(
        feePayerToken: Token.Active,
        transactionFeeInSol: BigInteger,
        accountCreationFeeInSol: BigInteger
    ): FeeAmount? {
        val transactionFeeSpl = amountsConverter.convertAmount(
            amountFrom = Constants.WRAPPED_SOL_MINT.toBase58Instance() to transactionFeeInSol,
            mintsToConvertTo = listOf(feePayerToken.mintAddress.toBase58Instance())
        )[feePayerToken.mintAddress.toBase58Instance()]
        val accountCreationFee = amountsConverter.convertAmount(
            amountFrom = Constants.WRAPPED_SOL_MINT.toBase58Instance() to accountCreationFeeInSol,
            mintsToConvertTo = listOf(feePayerToken.mintAddress.toBase58Instance())
        )[feePayerToken.mintAddress.toBase58Instance()]

        return if (transactionFeeSpl != null && accountCreationFee != null){
            FeeAmount(transactionFeeSpl, accountCreationFee)
        } else {
            null
        }
    }
}
