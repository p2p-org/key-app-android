package org.p2p.wallet.home.model

sealed class HomeElementItem {
    data class Shown(val token: Token.Active) : HomeElementItem()
    data class Hidden(val token: Token.Active, val state: VisibilityState) : HomeElementItem()
    data class Action(val state: VisibilityState, val isHiddenTokens: Boolean) : HomeElementItem()
    data class Banners(val banners: List<Banner>) : HomeElementItem()
}
