package org.p2p.wallet.newsend.model.smartselection

import java.math.BigDecimal
import org.p2p.core.token.Token
import org.p2p.wallet.feerelayer.model.FeeRelayerFee
import org.p2p.wallet.newsend.smartselection.strategy.FeePayerSelectionStrategy

sealed interface SmartSelectionState {
    object Cancelled : SmartSelectionState
    data class ReadyForSmartSelection(
        val strategies: LinkedHashSet<FeePayerSelectionStrategy>
    ) : SmartSelectionState

    data class SolanaFeeOnly(
        val feeInSol: FeeRelayerFee
    ) : SmartSelectionState

    data class NoFees(
        val sourceToken: Token.Active,
        val initialAmount: BigDecimal?
    ) : SmartSelectionState

    data class Failed(val e: Throwable) : SmartSelectionState
}
