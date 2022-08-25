package org.p2p.uikit.natives

import androidx.annotation.StringRes
import android.view.View
import com.google.android.material.snackbar.Snackbar
import org.p2p.uikit.utils.getString

fun interface SnackbarActionButtonClickListener {
    fun onActionButtonClicked(clickedSnackbar: Snackbar)
}

fun View.showSnackbarShort(snackbarText: CharSequence, onDismissed: () -> Unit = {}) {
    internalMakeSnackbar(
        this,
        text = snackbarText,
        buttonText = null,
        buttonAction = null,
        duration = Snackbar.LENGTH_SHORT
    )
        .addOnDismissedCallback(onDismissed)
        .show()
}

fun View.showSnackbarShort(@StringRes snackbarTextRes: Int, onDismissed: () -> Unit = {}) {
    internalMakeSnackbar(
        this,
        text = getString(snackbarTextRes),
        buttonText = null,
        buttonAction = null,
        duration = Snackbar.LENGTH_SHORT
    )
        .addOnDismissedCallback(onDismissed)
        .show()
}

fun View.showSnackbarShort(
    snackbarText: CharSequence,
    actionButtonText: CharSequence,
    actionButtonListener: SnackbarActionButtonClickListener
) {
    internalMakeSnackbar(
        this,
        text = snackbarText,
        buttonText = actionButtonText,
        buttonAction = actionButtonListener,
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
