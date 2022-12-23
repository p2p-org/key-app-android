package org.p2p.wallet.common.mvp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import org.p2p.uikit.natives.showSnackbarShort
import org.p2p.wallet.R
import org.p2p.wallet.utils.getErrorMessage
import org.p2p.wallet.utils.showErrorDialog
import org.p2p.wallet.utils.snackbar

abstract class BaseMvpBottomSheet<V : MvpView, P : MvpPresenter<V>>(
    @LayoutRes val layoutRes: Int
) : BottomSheetDialogFragment(), MvpView {

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
        }
    }

    //region ErrorMessages
    override fun showErrorMessage(e: Throwable?) {
        showErrorDialog(e)
    }

    override fun showErrorMessage(@StringRes messageResId: Int) {
        showErrorDialog(messageRes = messageResId)
    }

    override fun showErrorSnackBar(messageResId: Int, actionResId: Int?, block: (() -> Unit)?) {
        snackbar {
            it.setMessage(getString(messageResId))
                .setIcon(R.drawable.ic_close_red)
                .setAction(actionResId, block)
        }
    }

    override fun showErrorSnackBar(message: String, actionResId: Int?, block: (() -> Unit)?) {
        snackbar {
            it.setMessage(message)
                .setIcon(R.drawable.ic_close_red)
                .setAction(actionResId, block)
        }
    }

    override fun showErrorSnackBar(e: Throwable, actionResId: Int?, block: (() -> Unit)?) {
        snackbar {
            it.setMessage(e.getErrorMessage { res -> getString(res) })
                .setIcon(R.drawable.ic_close_red)
                .setAction(actionResId, block)
        }
    }

    override fun showSuccessSnackBar(messageResId: Int, actionResId: Int?, block: (() -> Unit)?) {
        snackbar {
            it.setMessage(getString(messageResId))
                .setIcon(R.drawable.ic_done)
                .setAction(actionResId, block)
        }
    }

    override fun showSuccessSnackBar(message: String, actionResId: Int?, block: (() -> Unit)?) {
        snackbar {
            it.setMessage(message)
                .setIcon(R.drawable.ic_done)
                .setAction(actionResId, block)
        }
    }

    override fun showInfoSnackBar(message: String, iconResId: Int?, actionResId: Int?, actionBlock: (() -> Unit)?) {
        snackbar {
            it.setMessage(message)
                .setAction(actionResId, actionBlock)
            iconResId?.let { icon ->
                it.setIcon(icon)
            }
        }
    }
    //endregion

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
