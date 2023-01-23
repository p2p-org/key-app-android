package org.p2p.core.utils.insets

import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.View

val systemAndImeType: Int
    get() = WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()

fun WindowInsetsCompat.systemAndIme(): Insets = getInsets(systemAndImeType)
fun WindowInsetsCompat.ime(): Insets = getInsets(WindowInsetsCompat.Type.ime())
fun WindowInsetsCompat.systemBars(): Insets = getInsets(WindowInsetsCompat.Type.systemBars())

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
