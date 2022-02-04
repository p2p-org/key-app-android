package org.p2p.wallet.common.mvp

import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import org.p2p.wallet.utils.showErrorDialog

abstract class BaseMvpFragment<V : MvpView, P : MvpPresenter<V>>(
    @LayoutRes layoutRes: Int
) : BaseFragment(layoutRes), MvpView {

    abstract val presenter: P

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

    override fun showErrorMessage(messageRes: Int) {
        showErrorDialog(messageRes = messageRes)
    }

    override fun showSnackbarMessage(messageRes: Int, @DrawableRes iconRes: Int?) {
        showSnackbar(getString(messageRes), iconRes)
    }

    override fun showSnackbarMessage(message: String, iconRes: Int?) {
        showSnackbar(message, iconRes)
    }
}