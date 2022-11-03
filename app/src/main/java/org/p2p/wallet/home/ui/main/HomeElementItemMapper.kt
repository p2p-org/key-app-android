package org.p2p.wallet.home.ui.main

import org.p2p.wallet.R
import org.p2p.wallet.home.model.HomeElementItem
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.home.model.VisibilityState

class HomeElementItemMapper {

    fun mapToItems(
        tokens: List<Token.Active>,
        visibilityState: VisibilityState,
        isZerosHidden: Boolean
    ): List<HomeElementItem> {
        val groups: Map<Boolean, List<Token.Active>> = tokens.groupBy { it.isDefinitelyHidden(isZerosHidden) }

        val hiddenTokens = groups[true].orEmpty()
        val visibleTokens = groups[false].orEmpty()

        val result = mutableListOf<HomeElementItem>(HomeElementItem.Title(R.string.home_tokens))

        result += visibleTokens.map { HomeElementItem.Shown(it) }

        if (hiddenTokens.isNotEmpty()) {
            result += HomeElementItem.Action(visibilityState)
        }

        if (visibilityState.isVisible) {
            result += hiddenTokens.map { HomeElementItem.Hidden(it, visibilityState) }
        }

        return result
    }
}
