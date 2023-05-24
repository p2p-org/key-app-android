package org.p2p.wallet.common.ui.bottomsheet

import androidx.annotation.CallSuper
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.p2p.wallet.R
import org.p2p.wallet.databinding.DialogBaseDoneBinding
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withTextOrGone

/**
 * BaseDoneBottomSheet made to handle cases where you have Done button at them bottom
 * or X at top to close (if needed) to cover cases where you do not need logic and Presenter
 * you need to show some Info content or select something and return as a result with [setFragmentResult]
 **/
abstract class BaseDoneBottomSheet : BaseBottomSheet(R.layout.dialog_base_done) {

    companion object {
        const val ARG_TITLE = "ARG_TITLE"
        const val ARG_REQUEST_KEY = "ARG_REQUEST_KEY"
        const val ARG_RESULT_KEY = "ARG_RESULT_KEY"
    }

    private val title: String? by args(ARG_TITLE)
    protected val resultKey: String by args(ARG_RESULT_KEY)
    protected val requestKey: String by args(ARG_REQUEST_KEY)

    val baseDialogBinding: DialogBaseDoneBinding by viewBinding()

    @CallSuper
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val innerView = onCreateInnerView(inflater, container, savedInstanceState)
        baseDialogBinding.viewInner.addView(innerView)
        return baseDialogBinding.root
    }

    abstract fun onCreateInnerView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(baseDialogBinding) {
            textViewTitle.withTextOrGone(title)
            setCloseClickListener()
            setDoneClickListener()
        }
    }

    override fun getTheme(): Int = R.style.WalletTheme_BottomSheet_RoundedSnow

    protected open fun getResult(): Any? = null

    protected fun setDoneButtonVisibility(isVisible: Boolean) {
        baseDialogBinding.buttonDone.isVisible = isVisible
    }

    protected fun setCloseButtonVisibility(isVisible: Boolean) {
        baseDialogBinding.imageViewClose.isVisible = isVisible
    }

    private fun DialogBaseDoneBinding.setCloseClickListener() {
        imageViewClose.setOnClickListener {
            dismissAllowingStateLoss()
        }
    }

    private fun DialogBaseDoneBinding.setDoneClickListener() {
        buttonDone.setOnClickListener {
            setFragmentResult(requestKey, bundleOf(resultKey to getResult()))
            dismissAllowingStateLoss()
        }
    }
}
