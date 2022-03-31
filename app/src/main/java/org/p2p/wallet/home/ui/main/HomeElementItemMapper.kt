package org.p2p.wallet.home.ui.main

import org.p2p.wallet.home.model.HomeElementItem
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.home.model.TokenVisibility
import org.p2p.wallet.home.model.VisibilityState

class HomeElementItemMapper {
    fun mapToItem(
        tokens: List<Token.Active>,
        visibilityState: VisibilityState,
        isZerosHidden: Boolean
    ): List<HomeElementItem> {
        return tokens.map { token ->
            if (token.isSOL) return@map HomeElementItem.Shown(token)

            when (token.visibility) {
                TokenVisibility.SHOWN -> HomeElementItem.Shown(token)
                TokenVisibility.HIDDEN -> HomeElementItem.Hidden(token, visibilityState)
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
