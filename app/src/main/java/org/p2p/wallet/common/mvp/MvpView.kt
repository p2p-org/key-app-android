package org.p2p.wallet.common.mvp

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

interface MvpView {
    fun showErrorMessage(e: Throwable? = null)
    fun showErrorMessage(@StringRes messageResId: Int)
    fun showErrorSnackBar(message: String, @StringRes actionResId: Int? = null, block: (() -> Unit)? = null)
    fun showErrorSnackBar(
        @StringRes messageResId: Int,
        @StringRes actionResId: Int? = null,
        block: (() -> Unit)? = null
    )

    fun showSuccessSnackBar(message: String, @StringRes actionResId: Int? = null, block: (() -> Unit)? = null)
    fun showSuccessSnackBar(
        @StringRes messageResId: Int,
        @StringRes actionResId: Int? = null,
        block: (() -> Unit)? = null
    )

    fun showInfoSnackBar(
        message: String,
        @DrawableRes iconResId: Int? = null,
        @StringRes actionResId: Int? = null,
        actionBlock: (() -> Unit)? = null
    )
}
