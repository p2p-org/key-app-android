package org.p2p.wallet.main.model

sealed class TokenItem {
    data class Shown(val token: Token.Active) : TokenItem()
    data class Hidden(val token: Token.Active, val state: VisibilityState) : TokenItem()
    data class Action(val state: VisibilityState) : TokenItem()
}