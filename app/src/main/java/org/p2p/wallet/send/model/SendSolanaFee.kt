package org.p2p.wallet.send.model

import android.os.Parcelable
import timber.log.Timber
import java.math.BigDecimal
import java.math.BigInteger
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.p2p.core.token.Token
import org.p2p.core.utils.Constants.SOL_SYMBOL
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.fromLamports
import org.p2p.core.utils.isLessThan
import org.p2p.core.utils.isMoreThan
import org.p2p.core.utils.isZero
import org.p2p.core.utils.isZeroOrLess
import org.p2p.core.utils.orZero
import org.p2p.core.utils.subtractRatio
import org.p2p.core.utils.toLamports
import org.p2p.core.utils.toUsd
import org.p2p.wallet.feerelayer.model.FeePayerSelectionStrategy
import org.p2p.wallet.feerelayer.model.FeePayerSelectionStrategy.CORRECT_AMOUNT
import org.p2p.wallet.feerelayer.model.FeeRelayerFee
import org.p2p.wallet.send.model.FeePayerState.KeepSame
import org.p2p.wallet.send.model.FeePayerState.ReduceInputAmount
import org.p2p.wallet.send.model.FeePayerState.SwitchToSol
import org.p2p.wallet.send.model.FeePayerState.SwitchToSpl

/**
 * This class contains information about fees for transaction or for a new account creation
 * */
@Parcelize
data class SendSolanaFee constructor(
    val feePayerToken: Token.Active,
    val feeRelayerFee: FeeRelayerFee,
    val token2022TransferFee: BigInteger,
    val token2022TransferFeePercent: BigDecimal,
    val sourceToken: Token.Active,
    private val solToken: Token.Active?,
    private val alternativeFeePayerTokens: List<Token.Active>,
    private val supportedFeePayerTokens: List<Token.Active>? = null
) : Parcelable {

    @IgnoredOnParcel
    val sourceTokenSymbol: String
        get() = sourceToken.tokenSymbol

    @IgnoredOnParcel
    val isTransactionFree: Boolean
        get() = feeRelayerFee.transactionFeeInSol.isZeroOrLess()

    @IgnoredOnParcel
    val isAccountCreationFree: Boolean
        get() = feeRelayerFee.accountCreationFeeInSol.isZeroOrLess()

    @IgnoredOnParcel
    val accountCreationFormattedFee: String
        get() = "${accountCreationFeeDecimals.formatToken()} ${feePayerToken.tokenSymbol}"

    @IgnoredOnParcel
    val totalFee: String
        get() = if (isTransactionFree) accountCreationFormattedFee else summedFeeDecimalsFormatted

    @IgnoredOnParcel
    val feeUsd: BigDecimal?
        get() = accountCreationFeeDecimals.toUsd(feePayerToken)

    @IgnoredOnParcel
    val feePayerSymbol: String
        get() = feePayerToken.tokenSymbol

    @IgnoredOnParcel
    val transactionFullFee: String
        get() = "$transactionDecimals $feePayerSymbol ${approxTransactionFeeUsd.orEmpty()}"

    @IgnoredOnParcel
    val approxTransactionFeeUsd: String?
        get() = transactionDecimals.toUsd(feePayerToken)?.let { "(~$$it)" }

    @IgnoredOnParcel
    val accountCreationFeeUsd: String
        get() = "$accountCreationFeeDecimals $feePayerSymbol ${getApproxAccountCreationFeeUsd().orEmpty()}"

    @IgnoredOnParcel
    val summedFeeDecimalsFormatted: String
        get() = "${(totalFeeDecimals).formatToken()} $feePayerSymbol"

    @IgnoredOnParcel
    val summedFeeDecimalsUsd: String?
        get() = totalFeeDecimals.toUsd(feePayerToken)?.let { ("(~$$it)") }

    fun getApproxAccountCreationFeeUsd(withBraces: Boolean = true): String? =
        accountCreationFeeDecimals.toUsd(feePayerToken)?.let {
            if (withBraces) "(~$$it)" else "~$$it"
        }

    @IgnoredOnParcel
    val accountCreationFeeDecimals: BigDecimal
        get() {
            val amount = if (feePayerToken.isSOL) {
                feeRelayerFee.accountCreationFeeInSol
            } else {
                feeRelayerFee.accountCreationFeeInFeePayerToken
            }
            return amount.fromLamports(feePayerToken.decimals)
        }

    @IgnoredOnParcel
    val transactionDecimals: BigDecimal
        get() {
            val amount = if (feePayerToken.isSOL) {
                feeRelayerFee.transactionFeeInSol
            } else {
                feeRelayerFee.transactionFeeInFeePayerToken
            }

            return amount.fromLamports(feePayerToken.decimals)
        }

    @IgnoredOnParcel
    private val feePayerTotalLamports: BigInteger
        get() = feePayerToken.total.toLamports(feePayerToken.decimals)

    @IgnoredOnParcel
    val totalFeeDecimals: BigDecimal
        get() = accountCreationFeeDecimals + transactionDecimals.orZero()

    @IgnoredOnParcel
    val totalFeeDecimalsUsd: BigDecimal?
        get() = totalFeeDecimals.toUsd(feePayerToken)

    fun isEnoughToCoverExpenses(
        sourceTokenTotal: BigInteger,
        inputAmount: BigInteger,
        minRentExemption: BigInteger
    ): Boolean = when {
        // if source is SOL, then fee payer is SOL as well
        sourceTokenSymbol == SOL_SYMBOL -> {
            isEnoughSol(sourceTokenTotal, inputAmount, minRentExemption)
        }
        // assuming that source token is not SOL
        feePayerToken.isSOL -> {
            val totalInSol = feeRelayerFee.totalInSol
            sourceTokenTotal >= inputAmount && isEnoughSol(feePayerTotalLamports, totalInSol, minRentExemption)
        }
        // assuming that source token and fee payer are same
        sourceTokenSymbol == feePayerSymbol ->
            sourceTokenTotal >= inputAmount + feeRelayerFee.totalInSourceToken
        // assuming that source token and fee payer are different
        else ->
            feePayerToken.totalInLamports >= feeRelayerFee.totalInFeePayerToken
    }

    private fun isEnoughSol(
        sourceTokenTotal: BigInteger,
        inputAmount: BigInteger,
        minRentExemption: BigInteger
    ): Boolean =
        sourceTokenTotal == inputAmount + feeRelayerFee.totalInSol ||
            // added min required balance for SOL check
            (sourceTokenTotal - minRentExemption) >= inputAmount + feeRelayerFee.totalInSol

    fun isEnoughSolBalance() = solToken?.let { !it.totalInLamports.isLessThan(feeRelayerFee.totalInSol) } ?: false

    fun calculateFeePayerState(
        strategy: FeePayerSelectionStrategy,
        sourceTokenTotal: BigInteger,
        inputAmount: BigInteger
    ): FeePayerState {
        // don't do anything if amount is not entered or it is zero
        if (inputAmount.isZero()) {
            return KeepSame
        }

        val feePayerTokenCanCoverExpenses = feePayerToken.totalInLamports >= feeRelayerFee.totalInFeePayerToken
        val feePayerIsSourceToken = feePayerSymbol == sourceTokenSymbol
        val isNotSourceSol = sourceTokenSymbol != SOL_SYMBOL
        val isAllowedToCorrectAmount = strategy == CORRECT_AMOUNT && isNotSourceSol
        val totalNeeded = feeRelayerFee.totalInSourceToken + inputAmount
        val isInsufficientSolBalance = !isEnoughSolBalance()
        val shouldTryReduceAmount = isAllowedToCorrectAmount && (isInsufficientSolBalance || feePayerIsSourceToken)
        val hasAlternativeFeePayerTokens = alternativeFeePayerTokens.isNotEmpty()
        val isValidToSwitchOnSource = supportedFeePayerTokens?.contains(sourceToken) ?: true
        // if there is enough SPL token balance to cover amount and fee
        val shouldSwitchToSpl =
            isNotSourceSol &&
                sourceTokenTotal.isMoreThan(totalNeeded) &&
                isValidToSwitchOnSource

        Timber.i(
            buildString {
                appendLine("isNotSourceSol = $isNotSourceSol")
                appendLine("isAllowedToCorrectAmount = $isAllowedToCorrectAmount")
                appendLine("totalNeeded = $totalNeeded")
                appendLine("isInsufficientSolBalance = $isInsufficientSolBalance")
                appendLine("shouldTryReduceAmount = $shouldTryReduceAmount")
                appendLine("hasAlternativeFeePayerTokens = $hasAlternativeFeePayerTokens")
                appendLine("alternativeFeePayerTokens = ${alternativeFeePayerTokens.map(Token.Active::tokenSymbol)}")
                appendLine("isValidToSwitchOnSource = $isValidToSwitchOnSource")
                appendLine("shouldSwitchToSpl = $shouldSwitchToSpl")
                appendLine("feePayerToken = ${feePayerToken.tokenSymbol}")
                appendLine("feePayerTokenCanCoverExpenses = $feePayerTokenCanCoverExpenses")
                appendLine("feePayerIsSourceToken = $feePayerIsSourceToken")
            }
        )
        return when {
            feePayerTokenCanCoverExpenses && !feePayerIsSourceToken && !shouldTryReduceAmount -> {
                Timber.i("FeePayer: keep the same fee payer token")
                KeepSame
            }
            shouldSwitchToSpl && !shouldTryReduceAmount -> {
                Timber.i("FeePayer: switch to SPL (${sourceToken.tokenSymbol})")
                SwitchToSpl(sourceToken)
            }
            hasAlternativeFeePayerTokens && !shouldTryReduceAmount -> {
                Timber.i("FeePayer: switch to max by balance alternative token")
                SwitchToSpl(alternativeFeePayerTokens.maxBy { it.totalInUsd.orZero() })
            }
            // if there is not enough SPL token balance to cover amount and fee, then try to reduce input amount
            shouldTryReduceAmount && sourceTokenTotal.isLessThan(totalNeeded) -> {
                val desiredAmountMinusFees = sourceTokenTotal - feeRelayerFee.totalInSourceToken
                if (desiredAmountMinusFees.isZeroOrLess()) {
                    // if it's not enough money on balance to cover at least fees, selecting another fee payer token
                    if (hasAlternativeFeePayerTokens) {
                        SwitchToSpl(alternativeFeePayerTokens.maxBy { it.totalInUsd.orZero() })
                    } else {
                        SwitchToSol
                    }
                } else {
                    // balance is sufficient to cover fees, calculating how much money can be sent
                    val desiredAmountMinusTransferFee = desiredAmountMinusFees
                        .toBigDecimal()
                        .subtractRatio(token2022TransferFeePercent.multiply(BigDecimal("0.01")))
                        // it rounds down to the nearest integer, so 100% accuracy is not guaranteed
                        .toBigInteger()

                    ReduceInputAmount(desiredAmountMinusTransferFee)
                }
            }
            else -> {
                SwitchToSol
            }
        }
    }
}
