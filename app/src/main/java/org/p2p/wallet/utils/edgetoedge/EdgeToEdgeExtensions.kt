@file:Suppress("DEPRECATION")

package org.p2p.wallet.utils.edgetoedge

import android.app.Activity
import android.app.Dialog
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import org.p2p.wallet.R
import timber.log.Timber

/**
 * Declares a set of view fitting rules and requests [android.view.WindowInsets] to be
 * applied to the view hierarchy. The library will fit the views according to their
 * fitting rules each time [android.view.WindowInsets] are applied. The set is usually declared in
 * the `Fragment.onViewCreated()` callback, but it can be re-declared at any time later,
 * for instance after applying a [androidx.constraintlayout.widget.ConstraintSet].
 * Each declaration adds new or overwrites already existing view fitting rules. For removing
 * a fitting rule [EdgeToEdgeBuilder.unfit] method can be used.
 *
 * ```
 * override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
 *     super.onViewCreated(view, savedInstanceState)
 *     edgeToEdge {
 *         appbar.fit { Edge.Top }
 *         recycler.fit { Edge.Bottom }
 *     }
 * }
 * ```
 */
inline fun Fragment.edgeToEdge(block: EdgeToEdgeBuilder.() -> Unit) {
    requireActivity().window.decorView.redispatchWindowInsetsToAllChildren()
    EdgeToEdgeBuilder(requireView()) {
        return@EdgeToEdgeBuilder isVisibleTopFragment()
    }.also(block).build()
}

inline fun DialogFragment.edgeToEdgeDialog(block: EdgeToEdgeBuilder.() -> Unit) {
    dialog?.window?.decorView?.redispatchWindowInsetsToAllChildren()
    EdgeToEdgeBuilder(requireView()) {
        return@EdgeToEdgeBuilder isVisibleTopFragment()
    }.also(block).build()
}

/**
 * Forces previously declared in [edgeToEdge] block views to fit the edges again
 * by re-applying their fitting rules. This function can be called, for example, after
 * applying a new `ConstraintSet` to an instance of `ConstraintLayout`.
 */
fun Fragment.fitEdgeToEdge() {
    requireView().dispatchWindowInsets()
}

inline fun Dialog.edgeToEdge(block: EdgeToEdgeBuilder.() -> Unit) {
    val contentView = findViewById<View>(android.R.id.content)
    window?.decorView?.redispatchWindowInsetsToAllChildren()
    window?.applyTranslucentFlag()
    EdgeToEdgeBuilder(contentView) { return@EdgeToEdgeBuilder isShowing }.also(block).build()
}

inline fun Activity.edgeToEdge(block: EdgeToEdgeBuilder.() -> Unit) {
    val contentView = findViewById<View>(R.id.content)
    window?.decorView?.redispatchWindowInsetsToAllChildren()
    EdgeToEdgeBuilder(contentView) { return@EdgeToEdgeBuilder true }.also(block).build()
}

/**
 * Fixes a problem with fitsSystemWindows where only the first view in viewHierarchy has a chance
 * to offset itself away from Status and Navigation bars. Typically when a view is defined
 * to fitsSystemWindows, it consumes those offsets, leaving no offsets for other views.
 * Listen for dispatch of WindowInsets and redispatch the offsets to all children. Even if the first
 * child consumes offsets, other children get the original offsets and can react accordingly as well.
 */
fun View.redispatchWindowInsetsToAllChildren() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        view as ViewGroup
        var consumed = false

        Timber.tag("insets").d(view::class.java.simpleName)
        view.children.forEach { child ->
            // Dispatch the insets to the child
            val childResult = ViewCompat.dispatchApplyWindowInsets(child, insets)
            // If the child consumed the insets, record it
            if (childResult.isConsumed) consumed = true
        }
        // If any of the children consumed the insets, return an appropriate value
        return@setOnApplyWindowInsetsListener when {
            consumed -> WindowInsetsCompat.CONSUMED
            else -> insets
        }
    }
}

fun Fragment.isVisibleTopFragment(): Boolean {
    return requireActivity().supportFragmentManager.findFragmentById(R.id.content) == this
}

fun Dialog.setOnApplyWindowInsetsListener(view: View, listener: (View, WindowInsetsCompat) -> WindowInsetsCompat) {
    window?.findViewById<View>(com.google.android.material.R.id.container)?.fitsSystemWindows = false
    window?.decorView?.redispatchWindowInsetsToAllChildren()
    window?.applyTranslucentFlag()
    ViewCompat.setOnApplyWindowInsetsListener(window?.decorView ?: view, listener)
}

/*
* For API < 23 It’s not possible to change the color of the status bar in android.
* The only thing you can set in your app is the status bar’s background color.
* Thus, we set translucent flag to set darker color to status bar and navigation bar
* */
fun Window.applyTranslucentFlag() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        setFlags(
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
        )
        setFlags(
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
        )
    }
}