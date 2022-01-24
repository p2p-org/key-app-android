package org.p2p.wallet.common.mvp

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.snackbar.Snackbar
import org.p2p.wallet.R
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

    override fun showSnackbarError(messageRes: Int) {
        showSnackbar(getString(messageRes))
    }

    override fun showSnackbarError(message: String) {
        showSnackbar(message)
    }

    private fun showSnackbar(message: String) {
        val snackbar = Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT)
        val view: View = snackbar.view
        val textView = view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textView.typeface = ResourcesCompat.getFont(requireContext(), R.font.manrope_medium)
        snackbar.show()
    }
}