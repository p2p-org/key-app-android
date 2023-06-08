package org.p2p.wallet.newsend.smartselection

import java.math.BigDecimal
import org.p2p.core.token.Token

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
