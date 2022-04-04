package org.p2p.wallet.home.model

sealed class VisibilityState {
    object Visible : VisibilityState()
    object Hidden : VisibilityState()

    fun toggle(): VisibilityState = if (this is Visible) Hidden else Visible

    fun toBoolean(): Boolean = when (this) {
        Visible -> true
        Hidden -> false
    }

    companion object {
        fun fromBoolean(visible: Boolean): VisibilityState = if (visible) Visible else Hidden
    }
}
