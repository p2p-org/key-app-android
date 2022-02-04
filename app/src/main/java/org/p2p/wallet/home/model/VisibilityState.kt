package org.p2p.wallet.home.model

sealed class VisibilityState {
    object Visible : VisibilityState()
    data class Hidden(val count: Int) : VisibilityState()
}