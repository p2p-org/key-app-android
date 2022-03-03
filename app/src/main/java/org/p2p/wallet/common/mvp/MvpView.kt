package org.p2p.wallet.common.mvp

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

interface MvpView {
    fun showErrorMessage(e: Throwable? = null)
    fun showErrorMessage(@StringRes messageRes: Int)
    fun showSnackbarMessage(@StringRes messageRes: Int, @DrawableRes iconRes: Int? = null)
    fun showSnackbarMessage(message: String, @DrawableRes iconRes: Int? = null)
}