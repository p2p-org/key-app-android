package org.p2p.wallet.newsend.smartselection

import java.math.BigDecimal
import org.p2p.core.token.Token
import org.p2p.wallet.newsend.model.smartselection.NoFeesData

/**
 * These are the states that can trigger the smart selection process.
 * Each trigger is handled by a trigger handler.
 * Each trigger may result a different list of strategies to be executed.
 * */
sealed interface SmartSelectionTrigger {

    data class Initialization(
        val initialToken: Token.Active,
        val initialAmount: BigDecimal?
    ) : SmartSelectionTrigger

    data class AmountChanged(
        val solToken: Token.Active,
        val sourceToken: Token.Active,
        val inputAmount: BigDecimal?
    ) : SmartSelectionTrigger

    data class SourceTokenChanged(
        val solToken: Token.Active,
        val newSourceToken: Token.Active,
        val inputAmount: BigDecimal?
    ) : SmartSelectionTrigger

    data class FeePayerManuallyChanged(
        val sourceToken: Token.Active,
        val newFeePayer: Token.Active,
        val inputAmount: BigDecimal
    ) : SmartSelectionTrigger

    data class MaxAmountEntered(
        val solToken: Token.Active,
        val sourceToken: Token.Active,
        val inputAmount: BigDecimal
    ) : SmartSelectionTrigger
}

fun SmartSelectionTrigger.getNoFeesData(): NoFeesData {
    val (sourceToken, initialAmount) = when (this) {
        is SmartSelectionTrigger.Initialization -> initialToken to initialAmount
        is SmartSelectionTrigger.AmountChanged -> sourceToken to inputAmount
        is SmartSelectionTrigger.SourceTokenChanged -> newSourceToken to inputAmount
        is SmartSelectionTrigger.FeePayerManuallyChanged -> sourceToken to inputAmount
        is SmartSelectionTrigger.MaxAmountEntered -> sourceToken to inputAmount
    }

    return NoFeesData(sourceToken, initialAmount)
}
