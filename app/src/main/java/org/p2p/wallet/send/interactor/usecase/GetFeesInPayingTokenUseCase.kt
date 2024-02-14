package org.p2p.wallet.send.interactor.usecase

import timber.log.Timber
import java.math.BigInteger
import org.p2p.core.crypto.Base58String
import org.p2p.core.crypto.toBase58Instance
import org.p2p.core.token.Token
import org.p2p.core.utils.Constants
import org.p2p.solanaj.core.FeeAmount
import org.p2p.token.service.converter.TokenServiceAmountsConverter

class GetFeesInPayingTokenUseCase(
    private val amountsConverter: TokenServiceAmountsConverter,
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

    /**
     * This function calculates how many target tokens we need in SOL equivalent to pay for the transaction
     */
    suspend fun execute(
        targetToken: Token.Active,
        transactionFeeInSol: BigInteger,
        accountCreationFeeInSol: BigInteger
    ): FeeAmount? {
        if (targetToken.isSOL) {
            return FeeAmount(transactionFeeInSol, accountCreationFeeInSol)
        }

        val transactionFeeInTargetToken = amountsConverter.convertAmount(
            amountFrom = Constants.WRAPPED_SOL_MINT.toBase58Instance() to transactionFeeInSol,
            mintsToConvertTo = listOf(targetToken.mintAddress.toBase58Instance())
        )[targetToken.mintAddress.toBase58Instance()]

        val accountCreationFeeInTargetToken = amountsConverter.convertAmount(
            amountFrom = Constants.WRAPPED_SOL_MINT.toBase58Instance() to accountCreationFeeInSol,
            mintsToConvertTo = listOf(targetToken.mintAddress.toBase58Instance())
        )[targetToken.mintAddress.toBase58Instance()]

        return if (transactionFeeInTargetToken != null && accountCreationFeeInTargetToken != null) {
            FeeAmount(transactionFeeInTargetToken, accountCreationFeeInTargetToken)
        } else {
            null
        }
    }
}
