package org.p2p.uikit.natives

import android.view.View
import com.google.android.material.snackbar.Snackbar

fun interface SnackbarActionButtonClickListener {
    fun onActionButtonClicked(clickedSnackbar: Snackbar)
}

fun View.showSnackbarShort(snackbarText: CharSequence) {
    internalMakeSnackbar(
        this,
        text = snackbarText,
        buttonText = null,
        buttonAction = null,
        duration = Snackbar.LENGTH_SHORT
    )
        .show()
}

fun View.showSnackbarShort(
    snackbarText: CharSequence,
    snackbarActionButtonText: CharSequence,
    snackbarActionButtonListener: SnackbarActionButtonClickListener
) {
    internalMakeSnackbar(
        this,
        text = snackbarText,
        buttonText = snackbarActionButtonText,
        buttonAction = snackbarActionButtonListener,
        duration = Snackbar.LENGTH_SHORT
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

private fun internalMakeSnackbar(
    view: View,
    text: CharSequence,
    buttonText: CharSequence?,
    buttonAction: SnackbarActionButtonClickListener?,
    duration: Int
): Snackbar {
    return Snackbar.make(view, text, duration).apply {
        if (buttonText != null && buttonAction != null) {
            setAction(buttonText) { buttonAction.onActionButtonClicked(this) }
        }
    }
}
