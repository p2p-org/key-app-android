package org.p2p.wallet.home.ui.main

import org.p2p.wallet.home.model.HomeElementItem
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.home.model.TokenVisibility
import org.p2p.wallet.home.model.VisibilityState

class HomeElementItemMapper {
    fun mapToItems(
        tokens: List<Token.Active>,
        visibilityState: VisibilityState,
        isZerosHidden: Boolean
    ): List<HomeElementItem> {
        return tokens.mapNotNull { token ->
            if (token.isSOL) return@mapNotNull HomeElementItem.Shown(token)

            when (token.visibility) {
                TokenVisibility.SHOWN ->
                    HomeElementItem.Shown(token)
                TokenVisibility.HIDDEN ->
                    if (visibilityState.isVisible) HomeElementItem.Hidden(token, visibilityState) else null
                TokenVisibility.DEFAULT ->
                    if (isZerosHidden && token.isZero) {
                        HomeElementItem.Hidden(token, visibilityState)
                    } else {
                        HomeElementItem.Shown(token)
                    }
            }
        }
    }
}
