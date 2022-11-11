package org.p2p.uikit.utils

import android.app.Activity
import android.content.Context
import android.view.View
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

fun View.showKeyboard() = (this.context as? Activity)?.showKeyboard()
fun View.hideKeyboard() = (this.context as? Activity)?.hideKeyboard()

fun Fragment.showKeyboard() = activity?.let(FragmentActivity::showKeyboard)
fun Fragment.hideKeyboard() = activity?.hideKeyboard()

fun Context.showKeyboard() = (this as? Activity)?.showKeyboard()
fun Context.hideKeyboard() = (this as? Activity)?.hideKeyboard()

fun Activity.showKeyboard() = WindowInsetsControllerCompat(window, window.decorView).show(WindowInsetsCompat.Type.ime())
fun Activity.hideKeyboard() = WindowInsetsControllerCompat(window, window.decorView).hide(WindowInsetsCompat.Type.ime())
