package org.p2p.wallet.common.mvp

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar
import org.p2p.core.common.TextContainer

interface MvpView {
    fun showErrorMessage(e: Throwable? = null)
    fun showErrorMessage(@StringRes messageResId: Int)

    @Deprecated(
        message = "Old design snackbar, use the UiKit snackbar",
        replaceWith = ReplaceWith("showUiKitSnackbar")
    )
    fun showErrorSnackBar(
        message: String,
        @StringRes actionResId: Int? = null,
        block: (() -> Unit)? = null
    )

    @Deprecated(
        message = "Old design snackbar, use the UiKit snackbar",
        replaceWith = ReplaceWith("showUiKitSnackbar")
    )
    fun showErrorSnackBar(
        @StringRes messageResId: Int,
        @StringRes actionResId: Int? = null,
        block: (() -> Unit)? = null
    )

    @Deprecated(
        message = "Old design snackbar, use the UiKit snackbar",
        replaceWith = ReplaceWith("showUiKitSnackbar")
    )
    fun showErrorSnackBar(
        e: Throwable,
        @StringRes actionResId: Int? = null,
        block: (() -> Unit)? = null
    )

    @Deprecated(
        message = "Old design snackbar, use the UiKit snackbar",
        replaceWith = ReplaceWith("showUiKitSnackbar")
    )
    fun showSuccessSnackBar(
        message: String,
        @StringRes actionResId: Int? = null,
        block: (() -> Unit)? = null
    )

    @Deprecated(
        message = "Old design snackbar, use the UiKit snackbar",
        replaceWith = ReplaceWith("showUiKitSnackbar")
    )
    fun showSuccessSnackBar(
        @StringRes messageResId: Int,
        @StringRes actionResId: Int? = null,
        block: (() -> Unit)? = null
    )

    @Deprecated(
        message = "Old design snackbar, use the UiKit snackbar",
        replaceWith = ReplaceWith("showUiKitSnackbar")
    )
    fun showInfoSnackBar(
        message: String,
        @DrawableRes iconResId: Int? = null,
        @StringRes actionResId: Int? = null,
        actionBlock: (() -> Unit)? = null
    )

    fun showToast(message: TextContainer)

    // new snackbar
    fun showUiKitSnackBar(
        message: String? = null,
        @StringRes messageResId: Int? = null,
        onDismissed: () -> Unit = {},
        @StringRes actionButtonResId: Int? = null,
        actionBlock: ((Snackbar) -> Unit)? = null,
    )
}
