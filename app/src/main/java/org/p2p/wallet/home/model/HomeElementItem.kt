package org.p2p.wallet.home.model

import org.p2p.core.token.Token

sealed class HomeElementItem {
    data class Claim(val token: Token.Eth, val isClaimEnabled: Boolean) : HomeElementItem()
    data class Shown(val token: Token.Active) : HomeElementItem()
    data class Hidden(val token: Token.Active, val state: VisibilityState) : HomeElementItem()
    data class Action(val state: VisibilityState) : HomeElementItem()
    data class Banner(val banner: HomeScreenBanner) : HomeElementItem()
    data class Title(val titleResId: Int) : HomeElementItem()
}
