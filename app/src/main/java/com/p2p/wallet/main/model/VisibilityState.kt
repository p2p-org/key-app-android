package com.p2p.wallet.main.model

sealed class VisibilityState {
    object Visible : VisibilityState()
    data class Hidden(val count: Int) : VisibilityState()
}