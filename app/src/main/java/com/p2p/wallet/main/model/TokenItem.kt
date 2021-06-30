package com.p2p.wallet.main.model

import com.p2p.wallet.token.model.Token

sealed class TokenItem {
    data class Shown(val token: Token) : TokenItem()
    data class Hidden(val token: Token, val state: VisibilityState) : TokenItem()
    data class Action(val state: VisibilityState) : TokenItem()
}