package org.p2p.wallet.common.mvp

import androidx.annotation.StringRes
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
}
