package org.p2p.wallet.common.mvp

import androidx.annotation.AnimRes
import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar
import org.p2p.core.common.TextContainer

interface BaseFragmentContract {
    fun overrideEnterAnimation(@AnimRes animation: Int)
    fun overrideExitAnimation(@AnimRes animation: Int)
    fun showToast(message: TextContainer)
    fun showUiKitSnackBar(
        message: String? = null,
        @StringRes messageResId: Int? = null,
        onDismissed: () -> Unit = {},
        @StringRes actionButtonResId: Int? = null,
        actionBlock: ((Snackbar) -> Unit)? = null,
    )
}
