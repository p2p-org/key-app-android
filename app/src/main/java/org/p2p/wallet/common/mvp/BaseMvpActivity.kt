package org.p2p.wallet.common.mvp

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
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

    override fun showErrorMessage(messageRes: Int) {
        showErrorDialog(messageRes = messageRes)
    }

    override fun showErrorMessage(e: Throwable?) {
        showErrorDialog(e)
    }

    protected fun showSnackBar(message: String) {
        snackbar { snackBar -> snackBar.setMessage(message) }
    }

    protected fun showSnackBar(message: String, @DrawableRes iconRes: Int) {
        snackbar { snackBar ->
            snackBar.setMessage(message)
            snackBar.setIcon(iconRes)
        }
    }

    protected fun showSnackBar(message: String, @DrawableRes iconRes: Int, actionText: String, block: () -> Unit) {
        snackbar { snackBar ->
            snackBar.setMessage(message)
            snackBar.setIcon(iconRes)
            snackBar.setAction(actionText, block)
        }
    }
}