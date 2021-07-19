package com.p2p.wallet.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnAttach
import androidx.core.view.doOnDetach
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView

fun View.requireActivity(): AppCompatActivity {
    var context: Context = context
    while (context is ContextWrapper) {
        if (context is AppCompatActivity) {
            return context
        }
        context = context.baseContext
    }

    throw IllegalStateException("View is not attached to any activity")
}

fun View.focusAndShowKeyboard() {
    /**
     * This is to be called when the window already has focus.
     */
    fun View.showTheKeyboardNow() {
        if (isFocused) {
            post {
                // We still post the call, just in case we are being notified of the windows focus
                // but InputMethodManager didn't get properly setup yet.
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }

    requestFocus()
    if (hasWindowFocus()) {
        // No need to wait for the window to get focus.
        showTheKeyboardNow()
    } else {
        val listener = ViewTreeObserver.OnWindowFocusChangeListener { hasFocus ->
            // This notification will arrive just before the InputMethodManager gets set up.
            if (hasFocus) showTheKeyboardNow()
        }
        // We need to wait until the window gets focus.
        viewTreeObserver.addOnWindowFocusChangeListener(listener)

        doOnDetach { viewTreeObserver.removeOnWindowFocusChangeListener(listener) }
    }
}

fun Activity.hideKeyboard() {
    currentFocus?.hideKeyboard()
}

fun View.hideKeyboard() {
    post {
        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(windowToken, 0)
    }
}

fun Context.showSoftKeyboard() {
    val windowToken = (this as Activity).findViewById<View>(android.R.id.content).rootView.windowToken
    val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputManager.toggleSoftInputFromWindow(windowToken, 0, 1)
}

fun View.showSoftKeyboard() {
    post {
        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .toggleSoftInputFromWindow(windowToken, 0, 1)
    }
}

fun Context.toast(text: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, text, duration).show()
}

fun Context.toast(@StringRes textRes: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, textRes, duration).show()
}

fun Fragment.toast(text: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(requireContext(), text, duration).show()
}

fun Fragment.toast(@StringRes textRes: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(requireContext(), textRes, duration).show()
}

fun Context.dip(value: Int): Int = dipF(value).toInt()
fun Context.dipF(value: Int): Float = value * resources.displayMetrics.density

fun View.dip(value: Int): Int = context.dip(value)
fun View.dipF(value: Int): Float = context.dipF(value)

fun Fragment.dip(value: Int): Int = requireContext().dip(value)
fun Fragment.dipF(value: Int): Float = requireContext().dipF(value)

fun RecyclerView.attachAdapter(adapter: RecyclerView.Adapter<*>) {
    doOnAttach { this.adapter = adapter }
    doOnDetach { this.adapter = null }
}