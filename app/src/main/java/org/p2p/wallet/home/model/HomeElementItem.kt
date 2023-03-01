package org.p2p.wallet.home.model

import org.p2p.core.token.Token

sealed class HomeElementItem {
    data class Claim(val claimStatus: Boolean) : HomeElementItem()
    data class Shown(val token: Token.Active) : HomeElementItem()
    data class Hidden(val token: Token.Active, val state: VisibilityState) : HomeElementItem()
    data class Action(val state: VisibilityState) : HomeElementItem()
    data class Banners(val banners: List<Banner>) : HomeElementItem()
    data class Title(val titleResId: Int) : HomeElementItem()
}
