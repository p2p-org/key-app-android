package org.p2p.wallet.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnAttach
import androidx.core.view.doOnDetach
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import org.p2p.wallet.common.ui.widget.SnackBar
import org.p2p.wallet.common.ui.widget.SnackBarView
import org.p2p.wallet.databinding.WidgetBottomSheetSnackbarBinding

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

fun View.createBitmap(): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    draw(canvas)
    return bitmap
}

fun Activity.hideKeyboard() {
    currentFocus?.hideKeyboard()
}

val View.keyboardIsVisible: Boolean
    get() = WindowInsetsCompat
        .toWindowInsetsCompat(rootWindowInsets)
        .isVisible(WindowInsetsCompat.Type.ime())

fun View.hideKeyboard() {
    post {
        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(windowToken, 0)
    }
}

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

fun RecyclerView.ViewHolder.requireContext(): Context = itemView.context

fun View.getString(@StringRes resourceId: Int): String =
    context.getString(resourceId)

fun View.getColor(@ColorRes colorRes: Int): Int =
    context.getColor(colorRes)

fun Fragment.getColor(@ColorRes colorRes: Int): Int =
    requireContext().getColor(colorRes)

fun Fragment.snackbar(action: (SnackBar) -> Unit) {
    val viewGroup = requireActivity().findViewById<ViewGroup>(android.R.id.content)
    viewGroup.snackbar(action)
}

fun AppCompatActivity.snackbar(action: (SnackBar) -> Unit) {
    val viewGroup = findViewById<View>(android.R.id.content) as ViewGroup
    viewGroup.snackbar(action)
}

fun ViewGroup.snackbar(action: (SnackBar) -> Unit) {
    val parent = findSuitableParent()
    val binding =
        WidgetBottomSheetSnackbarBinding.inflate(
            LayoutInflater.from(context), parent, false
        )

    val lp = CoordinatorLayout.LayoutParams(
        CoordinatorLayout.LayoutParams.MATCH_PARENT,
        CoordinatorLayout.LayoutParams.WRAP_CONTENT
    ).apply {
        bottomMargin = dip(24)
    }
    action.invoke(binding.snackbar)
    binding.snackbar.layoutParams = lp
    SnackBarView(this, binding.snackbar).apply { duration = Snackbar.LENGTH_SHORT }.show()
}
