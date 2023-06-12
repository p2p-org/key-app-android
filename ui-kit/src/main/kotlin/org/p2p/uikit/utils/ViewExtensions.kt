package org.p2p.uikit.utils

import androidx.annotation.ColorRes
import androidx.annotation.Dimension
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnAttach
import androidx.core.view.doOnDetach
import androidx.core.view.doOnLayout
import androidx.core.view.marginBottom
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.core.view.marginTop
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.Toast
import org.p2p.core.utils.showKeyboard

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

fun View.focusAndShowKeyboard(force: Boolean = false) {
    doOnLayout {
        if (force) {
            requestFocus()
            showKeyboard()
        } else {
            setOnFocusChangeListener { view, focus ->
                if (focus) {
                    showKeyboard()
                }
            }
            requestFocus()
        }
    }
}

fun View.createBitmap(): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    draw(canvas)
    return bitmap
}

val View.keyboardIsVisible: Boolean
    get() = WindowInsetsCompat
        .toWindowInsetsCompat(rootWindowInsets)
        .isVisible(WindowInsetsCompat.Type.ime())

fun View?.findSuitableParent(): ViewGroup {
    var view = this
    var fallback: ViewGroup = view as ViewGroup
    do {
        if (view is CoordinatorLayout) {
            return view
        } else if (view is FrameLayout) {
            if (view.id == android.R.id.content) {
                // If we've hit the decor content view, then we didn't find a CoL in the
                // hierarchy, so use it.
                return view
            } else {
                fallback = view
            }
        }

        if (view != null) {
            // Else, we will loop and crawl up the view hierarchy and try to find a parent
            val parent = view.parent
            view = if (parent is View) parent else null
        }
    } while (view != null)
    return fallback
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

fun dip(value: Int): Int = dipF(value).toInt()
fun dipF(value: Int): Float = value * Resources.getSystem().displayMetrics.density

fun RecyclerView.attachAdapter(adapter: RecyclerView.Adapter<*>) {
    doOnAttach { this.adapter = adapter }
    doOnDetach { this.adapter = null }
}

fun RecyclerView.ViewHolder.requireContext(): Context = itemView.context

fun View.getString(@StringRes resourceId: Int): String =
    context.getString(resourceId)

fun View.getColor(@ColorRes colorRes: Int): Int =
    context.getColor(colorRes)

fun View.getColorStateList(@ColorRes colorRes: Int): ColorStateList =
    context.getColorStateList(colorRes)

fun Fragment.getColor(@ColorRes colorRes: Int): Int =
    requireContext().getColor(colorRes)

fun View.setMargins(
    @Dimension(unit = Dimension.DP)
    left: Int = marginStart,
    @Dimension(unit = Dimension.DP)
    top: Int = marginTop,
    @Dimension(unit = Dimension.DP)
    right: Int = marginEnd,
    @Dimension(unit = Dimension.DP)
    bottom: Int = marginBottom
){
    updateLayoutParams<ViewGroup.MarginLayoutParams> {
        this.setMargins(left, top, right, bottom)
    }
}
