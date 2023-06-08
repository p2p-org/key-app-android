package org.p2p.wallet.newsend.smartselection.strategy

import java.math.BigDecimal
import java.math.BigInteger
import org.p2p.core.token.Token
import org.p2p.core.utils.isLessThan
import org.p2p.core.utils.toLamports
import org.p2p.wallet.feerelayer.model.FeeRelayerFee
import org.p2p.wallet.newsend.model.FeePayerState
import org.p2p.wallet.newsend.model.SendFatalError
import org.p2p.wallet.newsend.model.smartselection.FeePayerFailureReason

class ValidationStrategy(
    private val sourceToken: Token.Active,
    private val feePayerToken: Token.Active,
    private val minRentExemption: BigInteger,
    private val inputAmount: BigDecimal,
    private val fee: FeeRelayerFee
) : FeePayerSelectionStrategy {

    private val inputAmountLamports = inputAmount.toLamports(sourceToken.decimals)

    override fun isPayable(): Boolean = true

    override fun execute(): FeePayerState = when {
        !isEnoughBalance() -> {
            val reason = FeePayerFailureReason.InputExceeded(sourceToken)
            FeePayerState.Failure(reason)
        }
        !isEnoughToCoverExpenses() -> {
            val reason = FeePayerFailureReason.ExceededFee
            FeePayerState.Failure(reason)
        }
        else -> {
            val error = SendFatalError("Unknown error during validation")
            val reason = FeePayerFailureReason.CalculationError(error)
            FeePayerState.Failure(reason)
        }
    }

    private fun isEnoughBalance(): Boolean {
        val totalNeeded = inputAmount.toLamports(sourceToken.decimals)
        val totalInLamports = sourceToken.totalInLamports
        return totalNeeded.isLessThan(totalInLamports)
    }

    private fun isEnoughToCoverExpenses(): Boolean = when {
        // if source is SOL, then fee payer is SOL as well
        sourceToken.isSOL -> {
            val totalSolanaAvailable = sourceToken.totalInLamports - minRentExemption
            totalSolanaAvailable >= inputAmountLamports + fee.totalInSol
        }

        // assuming that source token is not SOL
        feePayerToken.isSOL -> {
            val sourceTokenTotal = sourceToken.totalInLamports
            val isEnoughToCoverInput = sourceTokenTotal >= inputAmountLamports
            val totalFeePayerAvailable = feePayerToken.totalInLamports - minRentExemption
            val isEnoughToCoverFees = totalFeePayerAvailable >= fee.totalInSol

            isEnoughToCoverInput && isEnoughToCoverFees
        }
        // assuming that source token and fee payer are same
        sourceToken.tokenSymbol == feePayerToken.tokenSymbol -> {
            val sourceTokenTotal = sourceToken.totalInLamports
            sourceTokenTotal >= inputAmountLamports + fee.totalInSpl
        }
        // assuming that source token and fee payer are different
        else -> {
            feePayerToken.totalInLamports >= fee.totalInSpl
        }
    }
}
