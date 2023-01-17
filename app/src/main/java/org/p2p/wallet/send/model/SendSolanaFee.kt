package org.p2p.wallet.send.model

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.p2p.core.token.Token
import org.p2p.core.utils.Constants.SOL_SYMBOL
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.fromLamports
import org.p2p.core.utils.isLessThan
import org.p2p.core.utils.isMoreThan
import org.p2p.core.utils.isZeroOrLess
import org.p2p.core.utils.orZero
import org.p2p.core.utils.scaleLong
import org.p2p.core.utils.toLamports
import org.p2p.core.utils.toUsd
import org.p2p.wallet.feerelayer.model.FeePayerSelectionStrategy
import org.p2p.wallet.feerelayer.model.FeePayerSelectionStrategy.CORRECT_AMOUNT
import org.p2p.wallet.feerelayer.model.FeeRelayerFee
import org.p2p.wallet.send.model.FeePayerState.ReduceInputAmount
import org.p2p.wallet.send.model.FeePayerState.SwitchToSol
import org.p2p.wallet.send.model.FeePayerState.UpdateFeePayer
import java.math.BigDecimal
import java.math.BigInteger

/**
 * This class contains information about fees for transaction or for a new account creation
 * */
@Parcelize
data class SendSolanaFee constructor(
    val feePayerToken: Token.Active,
    val sourceTokenSymbol: String,
    val feeRelayerFee: FeeRelayerFee,
    private val solToken: Token.Active?,
    private val availableTokens: List<Token.Active>?
) : Parcelable {

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
    val accountCreationFeeDecimals: BigDecimal =
        (if (feePayerToken.isSOL) feeRelayerFee.accountCreationFeeInSol else feeRelayerFee.accountCreationFeeInSpl)
            .fromLamports(feePayerToken.decimals)
            .scaleLong()

    @IgnoredOnParcel
    private val feePayerTotalLamports: BigInteger
        get() = feePayerToken.total.toLamports(feePayerToken.decimals)

    @IgnoredOnParcel
    private val transactionDecimals: BigDecimal =
        (if (feePayerToken.isSOL) feeRelayerFee.transactionFeeInSol else feeRelayerFee.transactionFeeInSpl)
            .fromLamports(feePayerToken.decimals)
            .scaleLong()

    @IgnoredOnParcel
    val totalFeeDecimals: BigDecimal
        get() = accountCreationFeeDecimals + transactionDecimals.orZero()

    @IgnoredOnParcel
    val totalFeeDecimalsUsd: BigDecimal?
        get() = totalFeeDecimals.toUsd(feePayerToken)

    fun isEnoughToCoverExpenses(
        sourceTokenTotal: BigInteger,
        inputAmount: BigInteger
    ): Boolean = when {
        // if source is SOL, then fee payer is SOL as well
        sourceTokenSymbol == SOL_SYMBOL ->
            sourceTokenTotal >= inputAmount + feeRelayerFee.totalInSol
        // assuming that source token is not SOL
        feePayerToken.isSOL ->
            sourceTokenTotal >= inputAmount && feePayerTotalLamports > feeRelayerFee.totalInSol
        // assuming that source token and fee payer are same
        sourceTokenSymbol == feePayerSymbol ->
            sourceTokenTotal >= inputAmount + feeRelayerFee.totalInSpl
        // assuming that source token and fee payer are different
        else ->
            feePayerToken.totalInLamports >= feeRelayerFee.totalInSpl
    }

    fun calculateFeePayerState(
        strategy: FeePayerSelectionStrategy,
        sourceTokenTotal: BigInteger,
        inputAmount: BigInteger
    ): FeePayerState {
        val isSourceSol = sourceTokenSymbol == SOL_SYMBOL
        val isAllowedToCorrectAmount = strategy == CORRECT_AMOUNT
        val totalNeeded = feeRelayerFee.totalInSpl + inputAmount
        val isEnoughSolBalance = solToken?.let { !it.totalInLamports.isLessThan(feeRelayerFee.totalInSol) } ?: false
        val shouldTryReduceAmount = isAllowedToCorrectAmount && !isSourceSol && !isEnoughSolBalance
        val hasAnotherTokens = !availableTokens.isNullOrEmpty()
        return when {
            // if there is not enough SPL token balance to cover amount and fee, then try to reduce input amount
            shouldTryReduceAmount && sourceTokenTotal.isLessThan(totalNeeded) -> {
                val diff = totalNeeded - sourceTokenTotal
                val desiredAmount = if (diff.isLessThan(inputAmount)) inputAmount - diff else null
                if (desiredAmount != null) ReduceInputAmount(desiredAmount) else SwitchToSol
            }
            // if there is enough SPL token balance to cover amount and fee
            !isSourceSol && sourceTokenTotal.isMoreThan(totalNeeded) -> UpdateFeePayer
            hasAnotherTokens -> {
                availableTokens?.firstOrNull { it.totalInLamports.isMoreThan(totalNeeded) }?.let { token ->
                    FeePayerState.SwitchToSpl(token)
                } ?: SwitchToSol
            }
            else -> SwitchToSol
        }
    }
}
