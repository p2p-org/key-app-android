package org.p2p.wallet.utils

import android.content.res.Resources

fun Float.toPx() = this * Resources.getSystem().displayMetrics.density
fun Int.toPx() = toFloat().toPx().toInt()

fun Float.toDp() = this / Resources.getSystem().displayMetrics.density
fun Int.toDp() = toFloat().toDp().toInt()
