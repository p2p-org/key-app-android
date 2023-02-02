package org.p2p.core.utils.insets

import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import android.view.View
import org.p2p.core.R

val systemAndImeType: Int
    get() = WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()

fun WindowInsetsCompat.systemAndIme(): Insets = getInsets(systemAndImeType)
fun WindowInsetsCompat.ime(): Insets = getInsets(WindowInsetsCompat.Type.ime())
fun WindowInsetsCompat.systemBars(): Insets = getInsets(WindowInsetsCompat.Type.systemBars())

inline fun Insets.consume(block: Insets.() -> Unit = {}): WindowInsetsCompat {
    block(this)
    return WindowInsetsCompat.CONSUMED
}

fun View.appleTopInsets(insets: Insets) {
    appleInsetPadding(top = insets.top)
}

fun View.appleBottomInsets(insets: Insets) {
    appleInsetPadding(bottom = insets.bottom)
}

fun View.appleInsetPadding(left: Int = 0, top: Int = 0, right: Int = 0, bottom: Int = 0) {
    val tagKey = R.id.initial_view_padding_tag_id
    val initialPadding = getTag(tagKey) as? InitialViewPadding ?: let {
        val paddings = recordInitialPaddingForView(this)
        setTag(tagKey, paddings)
        paddings
    }
    updatePadding(
        initialPadding.left + left,
        initialPadding.top + top,
        initialPadding.right + right,
        initialPadding.bottom + bottom
    )
}

fun View.doOnApplyWindowInsets(
    f: (view: View, insets: WindowInsetsCompat, initialPadding: InitialViewPadding) -> WindowInsetsCompat
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

data class InitialViewPadding(val left: Int, val top: Int, val right: Int, val bottom: Int)

fun recordInitialPaddingForView(view: View): InitialViewPadding = InitialViewPadding(
    view.paddingLeft, view.paddingTop, view.paddingRight, view.paddingBottom
)
