package org.p2p.wallet.home.model

sealed class VisibilityState {
    object Visible : VisibilityState()
    object Hidden : VisibilityState()
}