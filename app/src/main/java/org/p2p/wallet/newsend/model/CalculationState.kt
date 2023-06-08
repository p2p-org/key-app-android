package org.p2p.wallet.newsend.model

import org.p2p.core.utils.Constants.USD_READABLE_SYMBOL

sealed interface CalculationState {
    object Idle : CalculationState

    data class TokenUpdated(
        val switchInputSymbol: String,
        val currentInputSymbol: String,
        val fraction: Int,
        val approximateAmount: String
    ) : CalculationState

    data class AmountChanged(
        val approximateAmount: String,
        val isMaxButtonVisible: Boolean
    ) : CalculationState

    data class AmountReduced(
        val approximateAmount: String,
        val isMaxButtonVisible: Boolean,
        val newInputAmount: String
    ) : CalculationState

    data class MaxValueEntered(
        val approximateAmount: String,
        val newInputAmount: String,
        val isMaxButtonVisible: Boolean,
        val sourceTokenSymbol: String
    ) : CalculationState

    data class CurrencySwitched(
        val newInputAmount: String,
        val approximateAmount: String,
        val currentInputSymbol: String,
        val switchInputSymbol: String,
        val fraction: Int
    ) : CalculationState {
        val isFiat: Boolean = currentInputSymbol == USD_READABLE_SYMBOL
    }
}
