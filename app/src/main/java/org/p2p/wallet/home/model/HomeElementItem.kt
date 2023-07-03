package org.p2p.wallet.home.model

import java.math.BigDecimal
import org.p2p.core.crypto.Base58String
import org.p2p.core.token.Token

sealed class HomeElementItem {
    data class StrigaClaim(
        val amountAvailable: BigDecimal,
        val tokenName: String,
        val tokenMintAddress: Base58String,
        val tokenSymbol: String,
        val tokenIcon: String,
        val isClaimInProcess: Boolean
    ) : HomeElementItem()

    data class Claim(val token: Token.Eth, val isClaimEnabled: Boolean) : HomeElementItem()
    data class Shown(val token: Token.Active) : HomeElementItem()
    data class Hidden(val token: Token.Active, val state: VisibilityState) : HomeElementItem()
    data class Action(val state: VisibilityState) : HomeElementItem()
    data class Banner(val banner: HomeScreenBanner) : HomeElementItem()
    data class Title(val titleResId: Int) : HomeElementItem()
}
