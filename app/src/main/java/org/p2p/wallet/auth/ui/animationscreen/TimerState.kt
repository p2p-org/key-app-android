package org.p2p.wallet.auth.ui.animationscreen

import androidx.annotation.StringRes
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TimerState(
    @StringRes val titleRes: Int,
    val withProgress: Boolean = true
) : Parcelable
