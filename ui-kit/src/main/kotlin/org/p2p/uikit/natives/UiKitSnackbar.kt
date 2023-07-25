package org.p2p.uikit.natives

import androidx.coordinatorlayout.widget.CoordinatorLayout
import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber
import org.p2p.uikit.R
import org.p2p.uikit.utils.getColor

fun interface SnackbarActionButtonClickListener {
    fun onActionButtonClicked(clickedSnackbar: Snackbar)
}

fun View.showSnackbarShort(
    snackbarText: CharSequence,
    onDismissed: () -> Unit = {},
    style: UiKitSnackbarStyle = UiKitSnackbarStyle.BLACK,
    enableBottomNavOffset: Boolean = true,
    gravity: UiKitSnackbarGravity = UiKitSnackbarGravity.BOTTOM,
) {
    internalMakeSnackbar(
        this,
        text = snackbarText,
        buttonText = null,
        buttonAction = null,
        duration = Snackbar.LENGTH_SHORT,
        style = style,
        enableBottomNavOffset = enableBottomNavOffset,
        gravity = gravity
    )
        .addOnDismissedCallback(onDismissed)
        .show()
}

fun View.showSnackbarShort(
    snackbarText: CharSequence,
    actionButtonText: CharSequence,
    actionButtonListener: SnackbarActionButtonClickListener,
    enableBottomNavOffset: Boolean = true,
    gravity: UiKitSnackbarGravity = UiKitSnackbarGravity.BOTTOM,
    onDismissed: () -> Unit = {},
    style: UiKitSnackbarStyle = UiKitSnackbarStyle.BLACK
) {
    internalMakeSnackbar(
        this,
        text = snackbarText,
        buttonText = actionButtonText,
        buttonAction = actionButtonListener,
        duration = Snackbar.LENGTH_SHORT,
        style = style,
        enableBottomNavOffset = enableBottomNavOffset,
        gravity = gravity
    )
        .addOnDismissedCallback(onDismissed)
        .show()
}

fun View.showSnackbarLong(snackbarText: CharSequence) {
    internalMakeSnackbar(
        this,
        text = snackbarText,
        buttonText = null,
        buttonAction = null,
        duration = Snackbar.LENGTH_LONG
    )
        .show()
}

fun View.showSnackbarLong(
    snackbarText: CharSequence,
    snackbarActionButtonText: CharSequence,
    snackbarActionButtonListener: SnackbarActionButtonClickListener
) {
    internalMakeSnackbar(
        this,
        text = snackbarText,
        buttonText = snackbarActionButtonText,
        buttonAction = snackbarActionButtonListener,
        duration = Snackbar.LENGTH_LONG
    )
        .show()
}

fun View.showSnackbarIndefinite(
    snackbarText: CharSequence,
    snackbarActionButtonText: CharSequence,
    snackbarActionButtonListener: SnackbarActionButtonClickListener
): Snackbar = internalMakeSnackbar(
    this,
    text = snackbarText,
    buttonText = snackbarActionButtonText,
    buttonAction = snackbarActionButtonListener,
    duration = Snackbar.LENGTH_INDEFINITE
).apply { show() }

enum class UiKitSnackbarStyle {
    BLACK, WHITE
}

enum class UiKitSnackbarGravity(val androidValue: Int) {
    TOP(Gravity.TOP),
    BOTTOM(Gravity.BOTTOM)
}

private fun internalMakeSnackbar(
    view: View,
    text: CharSequence,
    buttonText: CharSequence?,
    buttonAction: SnackbarActionButtonClickListener?,
    duration: Int,
    style: UiKitSnackbarStyle = UiKitSnackbarStyle.BLACK,
    enableBottomNavOffset: Boolean = true,
    gravity: UiKitSnackbarGravity = UiKitSnackbarGravity.BOTTOM
): Snackbar {
    return Snackbar.make(view, text, duration).apply {
        if (buttonText != null && buttonAction != null) {
            setAction(buttonText) { buttonAction.onActionButtonClicked(this) }
        }

        val marginTop: Int = if (gravity == UiKitSnackbarGravity.TOP) {
            context.getStatusBarHeight()
        } else {
            0
        }
        val marginBottom: Int = if (enableBottomNavOffset) {
            context.resources.getDimension(R.dimen.bottom_navigation_height).toInt()
        } else {
            0
        }

        val horizontalMargin = context.resources.getDimension(R.dimen.ui_kit_average_horizontal_margin).toInt()
        val parentParams = this.view.layoutParams as MarginLayoutParams
        parentParams.setMargins(horizontalMargin, marginTop, horizontalMargin, marginBottom)
        this.view.layoutParams = parentParams.apply {
            when (this) {
                is CoordinatorLayout.LayoutParams -> {
                    this.gravity = gravity.androidValue
                }
                is FrameLayout.LayoutParams -> {
                    this.gravity = gravity.androidValue
                }
                is LinearLayout.LayoutParams -> {
                    this.gravity = gravity.androidValue
                }
                else -> {
                    Timber.w("Unsupported layout params type: ${this::class.java.simpleName} for snackbar gravity")
                }
            }
        }

        when (style) {
            UiKitSnackbarStyle.BLACK -> {
                setTextColor(view.getColor(R.color.text_snow))
                setBackgroundTint(view.getColor(R.color.bg_night))
            }
            UiKitSnackbarStyle.WHITE -> {
                setTextColor(view.getColor(R.color.text_night))
                setBackgroundTint(view.getColor(R.color.bg_snow))
            }
        }
    }
}

private fun Snackbar.addOnDismissedCallback(
    onDismissed: () -> Unit
): Snackbar {
    return this.addCallback(object : Snackbar.Callback() {
        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
            super.onDismissed(transientBottomBar, event)
            onDismissed()
        }
    })
}

@SuppressLint("DiscouragedApi")
@Suppress("InternalInsetResource")
private fun Context.getStatusBarHeight(): Int {
    var result = 0
    val resourceId = resources.getIdentifier(
        "status_bar_height",
        "dimen",
        "android"
    )
    if (resourceId > 0) {
        result = resources.getDimensionPixelSize(resourceId)
    }
    return result
}
