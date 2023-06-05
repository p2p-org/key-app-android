package org.p2p.wallet.send.smartselection.strategy

import org.p2p.wallet.send.model.FeePayerState

interface FeePayerSelectionStrategy {
    fun execute(): FeePayerState
}
