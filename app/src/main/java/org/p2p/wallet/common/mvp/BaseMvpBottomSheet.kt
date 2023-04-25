package org.p2p.wallet.common.mvp

import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import org.p2p.core.common.TextContainer
import org.p2p.uikit.natives.showSnackbarShort
import org.p2p.uikit.utils.toast
import org.p2p.wallet.common.ui.bottomsheet.BaseBottomSheet
import org.p2p.wallet.utils.showErrorDialog

abstract class BaseMvpBottomSheet<V : MvpView, P : MvpPresenter<V>>(
    @LayoutRes val layoutRes: Int
) : BaseBottomSheet(), MvpView {

    abstract val presenter: P

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(layoutRes, container, false)
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        @Suppress("UNCHECKED_CAST")
        presenter.attach(this as V)
        expandToFitAllContent()
    }

    @CallSuper
    override fun onDestroyView() {
        super.onDestroyView()
        presenter.detach()
    }

    protected fun expandToFitAllContent() {
        BottomSheetBehavior.from(requireView().parent as View).apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
        }
    }

    //region ErrorMessages
    override fun showErrorMessage(e: Throwable?) {
        showErrorDialog(e)
    }

    override fun showErrorMessage(@StringRes messageResId: Int) {
        showErrorDialog(messageRes = messageResId)
    }
    //endregion

    override fun showToast(message: TextContainer) {
        toast(message.getString(requireContext()))
    }

    override fun showUiKitSnackBar(
        message: String?,
        messageResId: Int?,
        onDismissed: () -> Unit,
        actionButtonResId: Int?,
        actionBlock: ((Snackbar) -> Unit)?
    ) {
        require(message != null || messageResId != null) {
            "Snackbar text must be set from `message` or `messageResId` params"
        }
        val snackbarText: String = message ?: messageResId?.let(::getString)!!
        val root = requireView().rootView
        if (actionButtonResId != null && actionBlock != null) {
            root.showSnackbarShort(
                snackbarText = snackbarText,
                actionButtonText = getString(actionButtonResId),
                actionButtonListener = actionBlock
            )
        } else {
            root.showSnackbarShort(
                snackbarText = snackbarText,
                onDismissed = onDismissed
            )
        }
    }
}
