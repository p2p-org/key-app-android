package org.p2p.wallet.common.mvp

import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.p2p.wallet.R
import org.p2p.wallet.utils.showErrorDialog
import org.p2p.wallet.utils.snackbar

abstract class BaseMvpBottomSheet<V : MvpView, P : MvpPresenter<V>>() :
    BottomSheetDialogFragment(), MvpView {

    abstract val presenter: P

    override fun showErrorMessage(e: Throwable?) {
        showErrorDialog(e)
    }

    override fun showErrorMessage(messageResId: Int) {
        showErrorDialog(messageRes = messageResId)
    }

    override fun showErrorSnackBar(messageResId: Int, actionResId: Int?, block: (() -> Unit)?) {
        snackbar(requireView()) { snackBar ->
            snackBar.setMessage(getString(messageResId))
            snackBar.setIcon(R.drawable.ic_close_red)
            if (actionResId != null && block != null) {
                snackBar.setAction(getString(actionResId), block)
            }
        }
    }

    override fun showErrorSnackBar(message: String, actionResId: Int?, block: (() -> Unit)?) {
        snackbar(requireView()) { snackBar ->
            snackBar.setMessage(message)
            snackBar.setIcon(R.drawable.ic_close_red)
            if (actionResId != null && block != null) {
                snackBar.setAction(getString(actionResId), block)
            }
        }
    }

    override fun showSuccessSnackBar(messageResId: Int, actionResId: Int?, block: (() -> Unit)?) {
        snackbar(requireView()) { snackBar ->
            snackBar.setMessage(getString(messageResId))
            snackBar.setIcon(R.drawable.ic_done)
            if (actionResId != null && block != null) {
                snackBar.setAction(getString(actionResId), block)
            }
        }
    }

    override fun showSuccessSnackBar(message: String, actionResId: Int?, block: (() -> Unit)?) {
        snackbar(requireView()) { snackBar ->
            snackBar.setMessage(message)
            snackBar.setIcon(R.drawable.ic_done)
            if (actionResId != null && block != null) {
                snackBar.setAction(getString(actionResId), block)
            }
        }
    }
}