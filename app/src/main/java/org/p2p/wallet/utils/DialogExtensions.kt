package org.p2p.wallet.utils

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import org.p2p.core.common.TextContainer
import org.p2p.core.utils.isConnectionError
import org.p2p.wallet.R
import org.p2p.wallet.common.ui.bottomsheet.ErrorBottomSheet
import org.p2p.wallet.common.ui.dialogs.InfoDialog

fun Fragment.showInfoDialog(
    @StringRes titleRes: Int? = null,
    @StringRes messageRes: Int,
    @StringRes primaryButtonRes: Int,
    @StringRes secondaryButtonRes: Int? = null,
    @ColorRes primaryButtonTextColor: Int? = null,
    primaryCallback: (() -> Unit)? = null,
    secondaryCallback: (() -> Unit)? = null,
    isCancelable: Boolean = true
) {
    InfoDialog.show(
        fragmentManager = childFragmentManager,
        titleRes = titleRes,
        subTitle = getString(messageRes),
        primaryButtonRes = primaryButtonRes,
        secondaryButtonRes = secondaryButtonRes,
        primaryButtonTextColor = primaryButtonTextColor,
        onPrimaryButtonClicked = { primaryCallback?.invoke() },
        onSecondaryButtonClicked = { secondaryCallback?.invoke() },
        isCancelable = isCancelable
    )
}

fun Fragment.showErrorDialog(
    @StringRes titleRes: Int = R.string.error_title,
    @DrawableRes iconRes: Int = R.drawable.ic_not_found,
    actionCallback: (() -> Unit)? = null,
    dismissCallback: (() -> Unit)? = null
) {
    ErrorBottomSheet.show(
        fragment = this,
        iconRes = iconRes,
        title = TextContainer(titleRes),
        primaryButtonRes = R.string.common_try_again,
        secondaryButtonRes = R.string.common_cancel,
        actionCallback = actionCallback,
        dismissCallback = dismissCallback
    )
}

fun FragmentActivity.showErrorDialog(
    @StringRes titleRes: Int = R.string.error_general_title,
    @DrawableRes iconRes: Int = R.drawable.ic_not_found,
    actionCallback: (() -> Unit)? = null,
    dismissCallback: (() -> Unit)? = null
) {
    ErrorBottomSheet.show(
        activity = this,
        iconRes = iconRes,
        primaryButtonRes = R.string.common_try_again,
        secondaryButtonRes = R.string.common_cancel,
        title = TextContainer(titleRes),
        actionCallback = actionCallback,
        dismissCallback = dismissCallback
    )
}

fun Fragment.showErrorDialog(title: String, dismissCallback: (() -> Unit)? = null) {
    ErrorBottomSheet.show(
        fragment = this,
        iconRes = R.drawable.ic_not_found,
        title = TextContainer(title),
        primaryButtonRes = R.string.common_try_again,
        secondaryButtonRes = R.string.common_cancel,
        actionCallback = null,
        dismissCallback = dismissCallback
    )
}

fun Fragment.showErrorDialog(throwable: Throwable? = null, dismissCallback: (() -> Unit)? = null) {
    ErrorBottomSheet.show(
        fragment = this,
        iconRes = throwable.getErrorIcon(),
        primaryButtonRes = R.string.common_try_again,
        secondaryButtonRes = R.string.common_cancel,
        title = TextContainer(throwable.getErrorMessage { res -> getString(res) }),
        actionCallback = dismissCallback,
        dismissCallback = null
    )
}

fun FragmentActivity.showErrorDialog(throwable: Throwable? = null) {
    ErrorBottomSheet.show(
        activity = this,
        iconRes = throwable.getErrorIcon(),
        primaryButtonRes = R.string.common_try_again,
        secondaryButtonRes = R.string.common_cancel,
        title = TextContainer(throwable.getErrorMessage { res -> getString(res) }),
        actionCallback = null,
        dismissCallback = null
    )
}

private fun Throwable?.getErrorIcon(): Int {
    return if (this?.isConnectionError() == true) {
        R.drawable.ic_cat
    } else {
        R.drawable.ic_not_found
    }
}

fun DialogFragment.showAllowingStateLoss(fragmentManager: FragmentManager) {
    val transaction = fragmentManager.beginTransaction()
    transaction.add(this, this::javaClass.name)
    transaction.commitAllowingStateLoss()
}
