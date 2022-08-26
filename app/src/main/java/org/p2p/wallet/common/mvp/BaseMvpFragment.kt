package org.p2p.wallet.common.mvp

import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.snackbar.Snackbar
import org.p2p.uikit.natives.showSnackbarShort
import org.p2p.uikit.utils.hideKeyboard
import org.p2p.uikit.utils.keyboardIsVisible
import org.p2p.wallet.R
import org.p2p.wallet.utils.getErrorMessage
import org.p2p.wallet.utils.showErrorDialog
import org.p2p.wallet.utils.snackbar

abstract class BaseMvpFragment<V : MvpView, P : MvpPresenter<V>>(
    @LayoutRes layoutRes: Int
) : BaseFragment(layoutRes), MvpView {

    abstract val presenter: P

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)?.apply {
            setOnClickListener { if (keyboardIsVisible) hideKeyboard() }
        }
        return view
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        @Suppress("UNCHECKED_CAST")
        presenter.attach(this as V)
    }

    @CallSuper
    override fun onDestroyView() {
        super.onDestroyView()
        presenter.detach()
    }

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
            it.setMessage(e.getErrorMessage(requireContext()))
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
        val root = requireActivity().findViewById<View>(android.R.id.content) as ViewGroup
        if (actionButtonResId != null && actionBlock != null) {
            root.showSnackbarShort(
                snackbarText = snackbarText,
                actionButtonText = getString(actionButtonResId),
                actionButtonListener = actionBlock,
                style = snackbarStyle
            )
        } else {
            root.showSnackbarShort(
                snackbarText = snackbarText,
                onDismissed = onDismissed,
                style = snackbarStyle
            )
        }
    }
}
