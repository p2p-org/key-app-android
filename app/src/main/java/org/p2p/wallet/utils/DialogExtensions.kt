package org.p2p.wallet.utils

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import org.p2p.wallet.R
import org.p2p.wallet.common.bottomsheet.ErrorBottomSheet
import org.p2p.wallet.common.bottomsheet.TextContainer

fun FragmentManager.showInfoDialog(
    @StringRes titleRes: Int,
    @StringRes messageRes: Int,
    @DrawableRes iconRes: Int = R.drawable.ic_common_error,
    actionCallback: (() -> Unit)? = null,
    dismissCallback: (() -> Unit)? = null
) {
    ErrorBottomSheet.show(
        fragmentManager = this,
        iconRes = iconRes,
        title = TextContainer(titleRes),
        message = TextContainer(messageRes),
        actionCallback = actionCallback,
        dismissCallback = dismissCallback
    )
}

fun Fragment.showInfoDialog(
    @StringRes titleRes: Int = R.string.error_title,
    @StringRes messageRes: Int = R.string.error_general_message,
    @DrawableRes iconRes: Int = R.drawable.ic_common_error,
    actionCallback: (() -> Unit)? = null,
    dismissCallback: (() -> Unit)? = null
) {
    ErrorBottomSheet.show(
        fragment = this,
        iconRes = iconRes,
        title = TextContainer(titleRes),
        message = TextContainer(messageRes),
        actionCallback = actionCallback,
        dismissCallback = dismissCallback
    )
}

fun FragmentActivity.showInfoDialog(
    @StringRes titleRes: Int = R.string.error_general_title,
    @StringRes messageRes: Int = R.string.error_general_message,
    @DrawableRes iconRes: Int = R.drawable.ic_common_error,
    actionCallback: (() -> Unit)? = null,
    dismissCallback: (() -> Unit)? = null
) {
    ErrorBottomSheet.show(
        activity = this,
        iconRes = iconRes,
        title = TextContainer(titleRes),
        message = TextContainer(messageRes),
        actionCallback = actionCallback,
        dismissCallback = dismissCallback
    )
}

fun Fragment.showInfoDialog(message: String, dismissCallback: (() -> Unit)? = null) {
    ErrorBottomSheet.show(
        fragment = this,
        iconRes = R.drawable.ic_common_error,
        title = TextContainer(R.string.error_title),
        message = TextContainer(message),
        actionCallback = null,
        dismissCallback = dismissCallback
    )
}

fun Fragment.showInfoDialog(throwable: Throwable? = null) {
    ErrorBottomSheet.show(
        fragment = this,
        iconRes = R.drawable.ic_common_error,
        title = TextContainer(R.string.error_title),
        message = TextContainer(throwable.getErrorMessage(requireContext())),
        actionCallback = null,
        dismissCallback = null
    )
}

fun FragmentActivity.showInfoDialog(throwable: Throwable? = null) {
    ErrorBottomSheet.show(
        activity = this,
        iconRes = R.drawable.ic_common_error,
        title = TextContainer(R.string.error_title),
        message = TextContainer(throwable.getErrorMessage(this)),
        actionCallback = null,
        dismissCallback = null
    )
}