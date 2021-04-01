package com.p2p.wowlet.fragment.dashboard.dialog.addcoin.util

import android.content.res.Resources
import android.util.DisplayMetrics
import android.util.TypedValue

fun Float.dpToPx(): Float {
    val displayMetrics: DisplayMetrics = Resources.getSystem().displayMetrics
    return this * displayMetrics.density
}

fun Float.pxToDp(): Float {
    return this.div(Resources.getSystem().displayMetrics.density)
}