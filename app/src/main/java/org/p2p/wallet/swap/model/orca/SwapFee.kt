package org.p2p.wallet.swap.model.orca

import java.math.BigDecimal
import java.math.BigInteger
import kotlinx.parcelize.IgnoredOnParcel
import org.p2p.core.token.Token
import org.p2p.core.utils.Constants.SOL_SYMBOL
import org.p2p.core.utils.SOL_DECIMALS
import org.p2p.core.utils.asApproximateUsd
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.fromLamports
import org.p2p.core.utils.isLessThan
import org.p2p.core.utils.isMoreThan
import org.p2p.core.utils.scaleToSix
import org.p2p.core.utils.toUsd
import org.p2p.wallet.feerelayer.model.FeePayerSelectionStrategy
import org.p2p.wallet.send.model.FeePayerState
import org.p2p.wallet.swap.model.FeeRelayerSwapFee

class SwapFee constructor(
    private val fee: FeeRelayerSwapFee,
    private val feePayerToken: Token.Active,
    private val sourceToken: Token.Active,
    destination: Token,
    private val solToken: Token.Active?
) {

    @IgnoredOnParcel
    val sourceTokenSymbol: String
        get() = sourceToken.tokenSymbol

    fun calculateFeePayerState(
        strategy: FeePayerSelectionStrategy,
        sourceTokenTotal: BigInteger,
        inputAmount: BigInteger
    ): FeePayerState {
        val isSourceSol = sourceTokenSymbol == SOL_SYMBOL
        val isAllowedToCorrectAmount = strategy == FeePayerSelectionStrategy.CORRECT_AMOUNT
        val totalNeeded = fee.feeInPayingToken + inputAmount
        val isEnoughSolBalance = solToken?.let { !it.totalInLamports.isLessThan(fee.feeInSol) } ?: false
        val shouldTryReduceAmount = isAllowedToCorrectAmount && !isSourceSol && !isEnoughSolBalance
        return when {
            // if there is not enough SPL token balance to cover amount and fee, then try to reduce input amount
            shouldTryReduceAmount && sourceTokenTotal.isLessThan(totalNeeded) -> {
                val diff = totalNeeded - sourceTokenTotal
                val desiredAmount = if (diff.isLessThan(inputAmount)) inputAmount - diff else null
                if (desiredAmount != null) FeePayerState.ReduceInputAmount(desiredAmount) else FeePayerState.SwitchToSol
            }
            // if there is enough SPL token balance to cover amount and fee
            !isSourceSol && sourceTokenTotal.isMoreThan(totalNeeded) ->
                FeePayerState.SwitchToSpl(sourceToken)
            else ->
                FeePayerState.SwitchToSol
        }
    }

    @Deprecated("Old")
    fun isEnoughToCoverExpenses(sourceTokenTotal: BigInteger, inputAmount: BigInteger): Boolean =
        when {
            // if source is SOL, then fee payer is SOL as well
            sourceTokenSymbol == SOL_SYMBOL ->
                sourceTokenTotal >= inputAmount + fee.feeInSol
            // assuming that source token is not SOL
            feePayerToken.isSOL ->
                sourceTokenTotal >= inputAmount && feePayerTotalLamports > fee.feeInSol
            // assuming that source token and fee payer are same
            else ->
                sourceTokenTotal >= inputAmount + fee.feeInPayingToken
        }

    val feeAmountInPayingToken: BigDecimal
        get() = fee.feeInPayingToken.fromLamports(feePayerToken.decimals).scaleToSix()

    val feeAmountInSol: BigDecimal
        get() = fee.feeInSol.fromLamports(SOL_DECIMALS).scaleToSix()

    val feePayerSymbol: String = feePayerToken.tokenSymbol

    val isFreeTransactionAvailable: Boolean = fee.isFreeTransactionAvailable

    val transactionFee: String
        get() = "${currentDecimals.formatToken()} ${feePayerToken.tokenSymbol}"

    val accountCreationToken: String =
        if (destination is Token.Other) destination.tokenSymbol else SOL_SYMBOL

    val accountCreationFee: String
        get() = "${accountCreationFeeDecimals.formatToken()} ${feePayerToken.tokenSymbol}"

    val accountCreationFeeUsd: String? =
        accountCreationFeeUsdDecimals?.asApproximateUsd()

    private val accountCreationFeeDecimals: BigDecimal
        get() {
            return if (feePayerToken.isSOL) {
                fee.feeInSol.fromLamports(feePayerToken.decimals)
            } else {
                fee.feeInPayingToken.fromLamports(feePayerToken.decimals)
            }
                .scaleToSix()
        }

    private val accountCreationFeeUsdDecimals: BigDecimal?
        get() = accountCreationFeeDecimals.toUsd(feePayerToken.rate)

    private val currentDecimals: BigDecimal =
        (if (feePayerToken.isSOL) fee.feeInSol else fee.feeInPayingToken)
            .fromLamports(feePayerToken.decimals)
            .scaleToSix()

    private val feePayerTotalLamports: BigInteger
        get() = feePayerToken.totalInLamports
}
