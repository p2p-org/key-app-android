package org.p2p.wallet.common.mvp

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import org.p2p.wallet.common.ui.widget.SnackBarView
import org.p2p.wallet.utils.showErrorDialog

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

    override fun showSnackbarMessage(message: String, iconRes: Int?) {
        showSnackbar(message, iconRes)
    }

    override fun showSnackbarMessage(messageRes: Int, iconRes: Int?) {
        showSnackbar(getString(messageRes), iconRes)
    }

    private fun showSnackbar(message: String, @DrawableRes iconRes: Int?) {
        SnackBarView.make(findViewById(android.R.id.content), message, iconRes)?.show()
    }
}