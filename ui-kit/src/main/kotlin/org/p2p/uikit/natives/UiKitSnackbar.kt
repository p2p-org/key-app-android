package org.p2p.uikit.natives

import android.view.View
import com.google.android.material.snackbar.Snackbar
import org.p2p.uikit.R
import org.p2p.uikit.utils.getColor

fun interface SnackbarActionButtonClickListener {
    fun onActionButtonClicked(clickedSnackbar: Snackbar)
}

fun View.showSnackbarShort(
    snackbarText: CharSequence,
    onDismissed: () -> Unit = {},
    style: UiKitSnackbarStyle = UiKitSnackbarStyle.BLACK
) {
    internalMakeSnackbar(
        this,
        text = snackbarText,
        buttonText = null,
        buttonAction = null,
        duration = Snackbar.LENGTH_SHORT,
        style = style
    )
        .addOnDismissedCallback(onDismissed)
        .show()
}

fun View.showSnackbarShort(
    snackbarText: CharSequence,
    actionButtonText: CharSequence,
    actionButtonListener: SnackbarActionButtonClickListener,
    style: UiKitSnackbarStyle = UiKitSnackbarStyle.BLACK
) {
    internalMakeSnackbar(
        this,
        text = snackbarText,
        buttonText = actionButtonText,
        buttonAction = actionButtonListener,
        duration = Snackbar.LENGTH_SHORT,
        style = style
    )
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

enum class UiKitSnackbarStyle {
    BLACK, WHITE
}

private fun internalMakeSnackbar(
    view: View,
    text: CharSequence,
    buttonText: CharSequence?,
    buttonAction: SnackbarActionButtonClickListener?,
    duration: Int,
    style: UiKitSnackbarStyle = UiKitSnackbarStyle.BLACK
): Snackbar {
    return Snackbar.make(view, text, duration).apply {
        if (buttonText != null && buttonAction != null) {
            setAction(buttonText) { buttonAction.onActionButtonClicked(this) }
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
