package org.p2p.wallet.newsend.smartselection.strategy

import org.p2p.wallet.newsend.model.FeePayerState

interface FeePayerSelectionStrategy {
    fun execute(): FeePayerState
}
