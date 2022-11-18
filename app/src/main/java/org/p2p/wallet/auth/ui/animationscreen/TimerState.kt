package org.p2p.wallet.auth.ui.animationscreen

import androidx.annotation.StringRes

data class TimerState(
    @StringRes val titleRes: Int,
    val withProgress: Boolean = true
)
