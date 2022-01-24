package org.p2p.wallet.common.mvp

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.R
import com.google.android.material.snackbar.Snackbar
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

    override fun showSnackbarError(messageRes: Int) {
        showSnackbar(getString(messageRes))
    }

    override fun showSnackbarError(message: String) {
        showSnackbar(message)
    }

    private fun showSnackbar(message: String) {
        val snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT)
        val view: View = snackbar.view
        val textView = view.findViewById<TextView>(R.id.snackbar_text)
        textView.typeface = Typeface.createFromAsset(assets, "manrope_medium.ttf")
        snackbar.show()
    }
}