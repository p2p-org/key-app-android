package org.p2p.wallet.utils

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import org.p2p.wallet.R
import org.p2p.wallet.common.ui.bottomsheet.ErrorBottomSheet
import org.p2p.wallet.common.ui.bottomsheet.TextContainer
import org.p2p.wallet.common.ui.dialogs.InfoDialog

fun Fragment.showInfoDialog(
    @StringRes titleRes: Int,
    @StringRes messageRes: Int,
    @StringRes primaryButtonRes: Int,
    @StringRes secondaryButtonRes: Int?,
    primaryCallback: (() -> Unit)? = null,
    secondaryCallback: (() -> Unit)? = null
) {
    InfoDialog.show(
        fragmentManager = childFragmentManager,
        titleRes = titleRes,
        subTitleRes = messageRes,
        primaryButtonRes = primaryButtonRes,
        secondaryButtonRes = secondaryButtonRes,
        onPrimaryButtonClicked = { primaryCallback?.invoke() },
        onSecondaryButtonClicked = { secondaryCallback?.invoke() }
    )
}

fun Fragment.showErrorDialog(
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

fun FragmentActivity.showErrorDialog(
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

fun Fragment.showErrorDialog(message: String, dismissCallback: (() -> Unit)? = null) {
    ErrorBottomSheet.show(
        fragment = this,
        iconRes = R.drawable.ic_common_error,
        title = TextContainer(R.string.error_title),
        message = TextContainer(message),
        actionCallback = null,
        dismissCallback = dismissCallback
    )
}

fun Fragment.showErrorDialog(throwable: Throwable? = null) {
    ErrorBottomSheet.show(
        fragment = this,
        iconRes = R.drawable.ic_common_error,
        title = TextContainer(R.string.error_title),
        message = TextContainer(throwable.getErrorMessage(requireContext())),
        actionCallback = null,
        dismissCallback = null
    )
}

fun FragmentActivity.showErrorDialog(throwable: Throwable? = null) {
    ErrorBottomSheet.show(
        activity = this,
        iconRes = R.drawable.ic_common_error,
        title = TextContainer(R.string.error_title),
        message = TextContainer(throwable.getErrorMessage(this)),
        actionCallback = null,
        dismissCallback = null
    )
}

fun DialogFragment.showAllowingStateLoss(fragmentManager: FragmentManager) {
    val transaction = fragmentManager.beginTransaction()
    transaction.add(this, this::javaClass.name)
    transaction.commitAllowingStateLoss()
}