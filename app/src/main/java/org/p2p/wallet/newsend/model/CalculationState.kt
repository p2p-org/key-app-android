package org.p2p.wallet.newsend.model

sealed interface CalculationState {
    object Idle : CalculationState
    data class CalculationCompleted(val aroundValue: String) : CalculationState
    data class InputFractionUpdate(val fraction: Int) : CalculationState
    data class LabelsUpdate(val switchSymbol: String, val mainSymbol: String) : CalculationState
    data class MaxValueEntered(
        val newInputAmount: String,
        val isMaxButtonVisible: Boolean,
        val sourceTokenSymbol: String
    ) : CalculationState

    data class MaxButtonVisible(val isMaxButtonVisible: Boolean) : CalculationState

    data class CurrencySwitched(
        val newInputAmount: String,
        val switchSymbol: String,
        val mainSymbol: String,
        val isFiat: Boolean
    ) : CalculationState
}
