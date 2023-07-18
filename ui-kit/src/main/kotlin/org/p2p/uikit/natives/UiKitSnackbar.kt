package org.p2p.uikit.natives

import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import com.google.android.material.snackbar.Snackbar
import org.p2p.uikit.R
import org.p2p.uikit.utils.getColor

fun interface SnackbarActionButtonClickListener {
    fun onActionButtonClicked(clickedSnackbar: Snackbar)
}

fun View.showSnackbarShort(
    snackbarText: CharSequence,
    onDismissed: () -> Unit = {},
    style: UiKitSnackbarStyle = UiKitSnackbarStyle.BLACK,
    enableBottomNavOffset: Boolean = true
) {
    internalMakeSnackbar(
        this,
        text = snackbarText,
        buttonText = null,
        buttonAction = null,
        duration = Snackbar.LENGTH_SHORT,
        style = style,
        enableBottomNavOffset = enableBottomNavOffset
    )
        .addOnDismissedCallback(onDismissed)
        .show()
}

fun View.showSnackbarShort(
    snackbarText: CharSequence,
    actionButtonText: CharSequence,
    actionButtonListener: SnackbarActionButtonClickListener,
    enableBottomNavOffset: Boolean = true,
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
        enableBottomNavOffset = enableBottomNavOffset
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

private fun internalMakeSnackbar(
    view: View,
    text: CharSequence,
    buttonText: CharSequence?,
    buttonAction: SnackbarActionButtonClickListener?,
    duration: Int,
    style: UiKitSnackbarStyle = UiKitSnackbarStyle.BLACK,
    enableBottomNavOffset: Boolean = true
): Snackbar {
    return Snackbar.make(view, text, duration).apply {
        if (buttonText != null && buttonAction != null) {
            setAction(buttonText) { buttonAction.onActionButtonClicked(this) }
        }

        val bottomMargin = if (enableBottomNavOffset) {
            context.resources.getDimension(R.dimen.bottom_navigation_height).toInt()
        } else {
            0
        }
        val horizontalMargin = context.resources.getDimension(R.dimen.ui_kit_average_horizontal_margin).toInt()
        val parentParams = this.view.layoutParams as MarginLayoutParams
        parentParams.setMargins(horizontalMargin, 0, horizontalMargin, bottomMargin)
        this.view.layoutParams = parentParams

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
