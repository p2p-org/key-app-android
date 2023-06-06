package org.p2p.wallet.newsend.model.smartselection

import org.p2p.core.token.Token

class SourceTokenStrategy(
    private val sourceToken: Token.Active
) : SmartSelectionStrategy {

    override fun selectFeePayer(): Token.Active {

    }
}
