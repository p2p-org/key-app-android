package org.p2p.wallet.send.model.smartselection

import org.p2p.core.token.Token

interface SmartSelectionStrategy {
    fun selectFeePayer() : Token.Active
}
