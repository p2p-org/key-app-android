package org.p2p.wallet.common.mvp

import androidx.annotation.StringRes

interface MvpView {
    fun showErrorMessage(e: Throwable? = null)
    fun showErrorMessage(@StringRes messageRes: Int)
    fun showSnackbarError(@StringRes messageRes: Int)
    fun showSnackbarError(message: String)
}