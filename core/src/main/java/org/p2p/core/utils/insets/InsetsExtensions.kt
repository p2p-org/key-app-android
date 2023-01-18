package org.p2p.core.utils.insets

import androidx.core.content.ContextCompat
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnDetach
import android.app.Activity
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager

val systemAndImeType: Int
    get() = WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()

fun WindowInsetsCompat.systemAndIme(): Insets = getInsets(systemAndImeType)
fun WindowInsetsCompat.ime(): Insets = getInsets(WindowInsetsCompat.Type.ime())
fun WindowInsetsCompat.systemBars(): Insets = getInsets(WindowInsetsCompat.Type.systemBars())

fun View.visibleKeyboard(isVisible: Boolean) {
    val imm: InputMethodManager? = ContextCompat.getSystemService(context, InputMethodManager::class.java)
    if (isVisible) imm?.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    else imm?.hideSoftInputFromWindow(windowToken, 0)
}

fun View.isKeyboardOpen(): Boolean {
    val insets = ViewCompat.getRootWindowInsets(this) ?: return false
    return insets.isVisible(WindowInsetsCompat.Type.ime())
}

fun View.setKeyboardListener(listener: ((Boolean) -> Unit)?) {
    // must set on view before WindowInsetsCompat.CONSUMED
    val rootView = (context as? Activity)?.window?.decorView?.rootView ?: return
    if (listener == null) {
        ViewCompat.setOnApplyWindowInsetsListener(rootView, null)
        return
    }
    doOnDetach { ViewCompat.setOnApplyWindowInsetsListener(rootView, null) }
    ViewCompat.setOnApplyWindowInsetsListener(rootView) { _, insets ->
        val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
        Log.i("Keyboard", if (imeVisible) "opened" else "closed")
        listener(imeVisible)
        insets
    }
}

fun View.doOnApplyWindowInsets(
    f: (view: View, insets: WindowInsetsCompat, initialPadding: InitialPadding) -> WindowInsetsCompat
) {
    // Create a snapshot of the view's padding state
    val initialPadding = recordInitialPaddingForView(this)
    // Set an actual OnApplyWindowInsetsListener which proxies to the given
    // lambda, also passing in the original padding state
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        f(v, insets, initialPadding)
    }
    // request some insets
    requestApplyInsetsWhenAttached()
}

fun View.requestApplyInsetsWhenAttached() {
    if (isAttachedToWindow) {
        // We're already attached, just request as normal
        requestApplyInsets()
    } else {
        // We're not attached to the hierarchy, add a listener to
        // request when we are
        addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                v.removeOnAttachStateChangeListener(this)
                v.requestApplyInsets()
            }

            override fun onViewDetachedFromWindow(v: View) = Unit
        })
    }
}

data class InitialPadding(val left: Int, val top: Int, val right: Int, val bottom: Int)

fun recordInitialPaddingForView(view: View) = InitialPadding(
    view.paddingLeft, view.paddingTop, view.paddingRight, view.paddingBottom
)
