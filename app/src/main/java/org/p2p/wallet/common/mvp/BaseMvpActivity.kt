package org.p2p.wallet.common.mvp

import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import org.p2p.uikit.natives.showSnackbarShort
import org.p2p.wallet.R
import org.p2p.wallet.utils.getErrorMessage
import org.p2p.wallet.utils.showErrorDialog
import org.p2p.wallet.utils.snackbar

abstract class BaseMvpActivity<V : MvpView, P : MvpPresenter<V>> : AppCompatActivity(), MvpView {

    abstract val presenter: P

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        @Suppress("UNCHECKED_CAST")
        presenter.attach(this as V)
    }

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
        presenter.detach()
    }

    override fun showErrorMessage(messageResId: Int) {
        showErrorDialog(messageRes = messageResId)
    }

    override fun showErrorMessage(e: Throwable?) {
        showErrorDialog(e)
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

    override fun showErrorSnackBar(e: Throwable, actionResId: Int?, block: (() -> Unit)?) {
        snackbar {
            it.setMessage(e.getErrorMessage(this))
                .setIcon(R.drawable.ic_close_red)
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

    override fun showUiKitSnackBar(message: String, onDismissed: () -> Unit) {
        val root = findViewById<View>(android.R.id.content) as ViewGroup
        root.showSnackbarShort(message, onDismissed)
    }

    override fun showUiKitSnackBar(messageResId: Int, onDismissed: () -> Unit) {
        val root = findViewById<View>(android.R.id.content) as ViewGroup
        root.showSnackbarShort(getString(messageResId), onDismissed)
    }

    override fun showUiKitSnackBar(
        message: String,
        actionButtonResId: Int,
        actionBlock: () -> Unit,
    ) {
        val root = findViewById<View>(android.R.id.content) as ViewGroup
        root.showSnackbarShort(
            snackbarText = message,
            actionButtonText = getString(actionButtonResId),
            actionButtonListener = { actionBlock.invoke() }
        )
    }

    override fun showUiKitSnackBar(
        messageResId: Int,
        actionButtonResId: Int,
        actionBlock: () -> Unit,
    ) {
        val root = findViewById<View>(android.R.id.content) as ViewGroup
        root.showSnackbarShort(
            snackbarText = getString(messageResId),
            actionButtonText = getString(actionButtonResId),
            actionButtonListener = { actionBlock.invoke() }
        )
    }
}
