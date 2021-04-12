package com.p2p.wallet.dashboard.ui.dialog.addcoin.util

import android.content.res.Resources
import android.util.DisplayMetrics

fun Float.dpToPx(): Float {
    val displayMetrics: DisplayMetrics = Resources.getSystem().displayMetrics
    return this * displayMetrics.density
}

fun Float.pxToDp(): Float {
    return this.div(Resources.getSystem().displayMetrics.density)
}