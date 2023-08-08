package org.p2p.wallet.newsend.model

import org.p2p.core.utils.Constants.USD_READABLE_SYMBOL

sealed interface CalculationEvent {
    object Idle : CalculationEvent

    data class TokenUpdated(
        val switchInputSymbol: String,
        val currentInputSymbol: String,
        val fraction: Int,
        val approximateAmount: String
    ) : CalculationEvent

    data class AmountChanged(
        val approximateAmount: String,
        val isMaxButtonVisible: Boolean
    ) : CalculationEvent

    // TODO: Will be implemented in SendService V2
//    data class AmountReduced(
//        val approximateAmount: String,
//        val isMaxButtonVisible: Boolean,
//        val newInputAmount: String
//    ) : CalculationEvent

    data class MaxValueEntered(
        val approximateAmount: String,
        val newInputAmount: String,
        val isMaxButtonVisible: Boolean,
        val sourceTokenSymbol: String
    ) : CalculationEvent

    data class CurrencySwitched(
        val newInputAmount: String,
        val approximateAmount: String,
        val currentInputSymbol: String,
        val switchInputSymbol: String,
        val fraction: Int
    ) : CalculationEvent {
        val isFiat: Boolean = currentInputSymbol == USD_READABLE_SYMBOL
    }
}
