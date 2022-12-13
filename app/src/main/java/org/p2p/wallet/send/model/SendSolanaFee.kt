package org.p2p.wallet.send.model

import android.os.Parcelable
import org.p2p.core.token.Token
import org.p2p.core.utils.Constants.SOL_SYMBOL
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.fromLamports
import org.p2p.core.utils.isLessThan
import org.p2p.core.utils.isMoreThan
import org.p2p.core.utils.isZeroOrLess
import org.p2p.core.utils.scaleMedium
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
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * This class contains information about fees for transaction or for a new account creation
 * */
@Parcelize
data class SendSolanaFee constructor(
    val feePayerToken: Token.Active,
    val sourceTokenSymbol: String,
    val feeRelayerFee: FeeRelayerFee,
    private val solToken: Token.Active?
) : Parcelable {

    @IgnoredOnParcel
    val isTransactionFree: Boolean
        get() = feeRelayerFee.transactionFeeInSol.isZeroOrLess()

    @IgnoredOnParcel
    val accountCreationFormattedFee: String
        get() = "${accountCreationFeeDecimals.formatToken()} ${feePayerToken.tokenSymbol}"

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
    val accountCreationFullFee: String
        get() = "$accountCreationFeeDecimals $feePayerSymbol ${approxAccountCreationFeeUsd.orEmpty()}"

    @IgnoredOnParcel
    val approxAccountCreationFeeUsd: String?
        get() = accountCreationFeeDecimals.toUsd(feePayerToken)?.let { "(~$$it)" }

    @IgnoredOnParcel
    val accountCreationFeeDecimals: BigDecimal =
        (if (feePayerToken.isSOL) feeRelayerFee.accountCreationFeeInSol else feeRelayerFee.accountCreationFeeInSpl)
            .fromLamports(feePayerToken.decimals)
            .scaleMedium()

    @IgnoredOnParcel
    private val feePayerTotalLamports: BigInteger
        get() = feePayerToken.total.toLamports(feePayerToken.decimals)

    @IgnoredOnParcel
    private val transactionDecimals: BigDecimal =
        (if (feePayerToken.isSOL) feeRelayerFee.transactionFeeInSol else feeRelayerFee.transactionFeeInSpl)
            .fromLamports(feePayerToken.decimals)
            .scaleMedium()

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
        else ->
            sourceTokenTotal >= inputAmount + feeRelayerFee.totalInSpl
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
        return when {
            // if there is not enough SPL token balance to cover amount and fee, then try to reduce input amount
            shouldTryReduceAmount && sourceTokenTotal.isLessThan(totalNeeded) -> {
                val diff = totalNeeded - sourceTokenTotal
                val desiredAmount = if (diff.isLessThan(inputAmount)) inputAmount - diff else null
                if (desiredAmount != null) ReduceInputAmount(desiredAmount) else SwitchToSol
            }
            // if there is enough SPL token balance to cover amount and fee
            !isSourceSol && sourceTokenTotal.isMoreThan(totalNeeded) ->
                UpdateFeePayer
            else ->
                SwitchToSol
        }
    }
}
