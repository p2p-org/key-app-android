package com.p2p.wowlet.fragment.backupwallat.secretkeys.utils

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import kotlin.math.roundToInt


fun Context.showSoftKeyboard() {
    val windowToken = (this as Activity).findViewById<View>(android.R.id.content).rootView.windowToken
    getInputMethodManager().toggleSoftInputFromWindow(windowToken, 0, 1)
}

fun Context.hideSoftKeyboard(fragment: Fragment) {
    getInputMethodManager().hideSoftInputFromWindow(fragment.view?.rootView?.windowToken, 0)
}

fun Context.hideSoftKeyboard() {
    val windowToken = (this as Activity).findViewById<View>(android.R.id.content).rootView.windowToken
    getInputMethodManager().hideSoftInputFromWindow(windowToken, 0)
}

private fun Context.getInputMethodManager() = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager




























