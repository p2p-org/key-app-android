package org.p2p.wallet.newsend.model

import java.math.BigDecimal
import org.p2p.core.token.Token
import org.p2p.core.utils.orZero
import org.p2p.wallet.feerelayer.model.FeeRelayerFee
import org.p2p.wallet.newsend.smartselection.FeePayerFailureReason

sealed interface FeePayerState {

    object Idle : FeePayerState

    data class FreeTransaction(
        val sourceToken: Token.Active,
        val initialAmount: BigDecimal? = null
    ) : FeePayerState {
        val formattedAmount: String = "${initialAmount.orZero().toPlainString()} ${sourceToken.tokenSymbol}"
    }

    data class CalculationSuccess(
        val sourceToken: Token.Active,
        val feePayerToken: Token.Active,
        val fee: FeeRelayerFee,
        val inputAmount: BigDecimal?
    ) : FeePayerState {
        val formattedAmount: String = "${inputAmount.orZero().toPlainString()} ${sourceToken.tokenSymbol}"
    }

    data class ReduceAmount(
        val sourceToken: Token.Active,
        val feePayerToken: Token.Active,
        val fee: FeeRelayerFee,
        val newInputAmount: BigDecimal
    ) : FeePayerState {
        val formattedAmount = "${newInputAmount.toPlainString()} ${sourceToken.tokenSymbol}"
    }

    object NoStrategiesFound : FeePayerState // this shouldn't happen at all

    data class Failure(val reason: FeePayerFailureReason) : FeePayerState

//    fun isTransactionFree(): Boolean {
//        if (this is FreeTransaction) {
//            return true
//        }
//
//        if (this is CalculationSuccess) {
//            return this.fee.isFree()
//        }
//
//        return false
//    }

    fun isEmptyInput(): Boolean {
        if (this is FreeTransaction) {
            return this.initialAmount == null
        }

        if (this is CalculationSuccess) {
            return this.inputAmount == null
        }

        return false
    }
}
