package org.p2p.wallet.common.mvp

import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar
import org.p2p.core.common.TextContainer

interface MvpView {
    fun showErrorMessage(e: Throwable? = null)
    fun showErrorMessage(@StringRes messageResId: Int)

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
