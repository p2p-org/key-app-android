package org.p2p.wallet.send.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.p2p.wallet.feerelayer.model.FeePayerSelectionStrategy
import org.p2p.wallet.feerelayer.model.FeePayerSelectionStrategy.CORRECT_AMOUNT
import org.p2p.core.token.Token
import org.p2p.wallet.send.model.FeePayerState.ReduceInputAmount
import org.p2p.wallet.send.model.FeePayerState.SwitchToSol
import org.p2p.wallet.send.model.FeePayerState.UpdateFeePayer
import org.p2p.core.utils.Constants.SOL_SYMBOL
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.fromLamports
import org.p2p.core.utils.isLessThan
import org.p2p.core.utils.isMoreThan
import org.p2p.core.utils.scaleMedium
import org.p2p.core.utils.toLamports
import org.p2p.core.utils.toUsd
import java.math.BigDecimal
import java.math.BigInteger

sealed interface SendFee : Parcelable {

    val feePayerToken: Token.Active
    val feePayerSymbol: String
    val formattedFee: String
    val sourceTokenSymbol: String
    val feeUsd: BigDecimal?
    val feeDecimals: BigDecimal

    fun isEnoughToCoverExpenses(sourceTokenTotal: BigInteger, inputAmount: BigInteger): Boolean

    @Parcelize
    class RenBtcFee(
        override val feePayerToken: Token.Active,
        private val feeLamports: BigInteger
    ) : SendFee {

        override val sourceTokenSymbol: String
            get() = feePayerToken.tokenSymbol

        override val formattedFee: String
            get() = "${fee.formatToken()} ${feePayerToken.tokenSymbol}"

        override val feeDecimals: BigDecimal
            get() = fee

        override val feeUsd: BigDecimal?
            get() = fee.toUsd(feePayerToken)

        override val feePayerSymbol: String
            get() = feePayerToken.tokenSymbol

        override fun isEnoughToCoverExpenses(
            sourceTokenTotal: BigInteger,
            inputAmount: BigInteger
        ): Boolean =
            sourceTokenTotal > inputAmount + feeLamports

        val fullFee: String
            get() = "$fee ${feePayerToken.tokenSymbol} ${approxFeeUsd.orEmpty()}"

        val approxFeeUsd: String?
            get() = fee.toUsd(feePayerToken)?.let { "(~$$it)" }

        val fee: BigDecimal
            get() = feeLamports.fromLamports(feePayerToken.decimals).scaleMedium()
    }

    /*
    * feeLamports is only in SOL
    * */
    @Parcelize
    class SolanaFee(
        override val sourceTokenSymbol: String,
        override val feePayerToken: Token.Active,
        private val solToken: Token.Active?,
        private val feeInSol: BigInteger,
        private val feeInPayingToken: BigInteger
    ) : SendFee {

        override val feeDecimals: BigDecimal
            get() = currentDecimals.scaleMedium()

        override val formattedFee: String
            get() = "${currentDecimals.formatToken()} ${feePayerToken.tokenSymbol}"

        override val feeUsd: BigDecimal?
            get() = currentDecimals.toUsd(feePayerToken)

        override val feePayerSymbol: String
            get() = feePayerToken.tokenSymbol

        override fun isEnoughToCoverExpenses(
            sourceTokenTotal: BigInteger,
            inputAmount: BigInteger
        ): Boolean = when {
            // if source is SOL, then fee payer is SOL as well
            sourceTokenSymbol == SOL_SYMBOL ->
                sourceTokenTotal >= inputAmount + feeInSol
            // assuming that source token is not SOL
            feePayerToken.isSOL ->
                sourceTokenTotal >= inputAmount && feePayerTotalLamports > feeInSol
            // assuming that source token and fee payer are same
            else ->
                sourceTokenTotal >= inputAmount + feeInPayingToken
        }

        fun calculateFeePayerState(
            strategy: FeePayerSelectionStrategy,
            sourceTokenTotal: BigInteger,
            inputAmount: BigInteger
        ): FeePayerState {
            val isSourceSol = sourceTokenSymbol == SOL_SYMBOL
            val isAllowedToCorrectAmount = strategy == CORRECT_AMOUNT
            val totalNeeded = feeInPayingToken + inputAmount
            val isEnoughSolBalance = solToken?.let { !it.totalInLamports.isLessThan(feeInSol) } ?: false
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

        val accountCreationFullFee: String
            get() = "$feeDecimals $feePayerSymbol ${approxAccountCreationFeeUsd.orEmpty()}"

        val approxAccountCreationFeeUsd: String?
            get() = feeDecimals.toUsd(feePayerToken)?.let { "(~$$it)" }

        private val feePayerTotalLamports: BigInteger
            get() = feePayerToken.total.toLamports(feePayerToken.decimals)

        private val currentDecimals: BigDecimal =
            (if (feePayerToken.isSOL) feeInSol else feeInPayingToken)
                .fromLamports(feePayerToken.decimals)
                .scaleMedium()
    }
}
