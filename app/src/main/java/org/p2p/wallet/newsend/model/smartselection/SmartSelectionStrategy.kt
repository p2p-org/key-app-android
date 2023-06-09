package org.p2p.wallet.newsend.model.smartselection

import org.p2p.core.token.Token

interface SmartSelectionStrategy {
    fun selectFeePayer(): Token.Active
}
