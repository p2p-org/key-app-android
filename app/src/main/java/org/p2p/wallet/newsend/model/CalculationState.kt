package org.p2p.wallet.newsend.model

sealed interface CalculationState {
    object Idle : CalculationState
    data class CalculationCompleted(val aroundValue: String) : CalculationState
    data class InputFractionUpdate(val fraction: Int) : CalculationState
    data class LabelsUpdate(val switchSymbol: String, val mainSymbol: String) : CalculationState
}
