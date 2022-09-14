package org.p2p.wallet.common.ui.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.p2p.wallet.R
import org.p2p.wallet.databinding.DialogBaseDoneBinding
import org.p2p.wallet.utils.args

abstract class BaseDoneBottomSheet : BottomSheetDialogFragment() {

    companion object {
        const val ARG_TITLE = "ARG_TITLE"
        const val ARG_REQUEST_KEY = "ARG_REQUEST_KEY"
        const val ARG_RESULT_KEY = "ARG_RESULT_KEY"
    }

    private val title: String by args(ARG_TITLE)
    protected val resultKey: String by args(ARG_RESULT_KEY)
    protected val requestKey: String by args(ARG_REQUEST_KEY)

    lateinit var baseDialogBinding: DialogBaseDoneBinding

    @CallSuper
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        baseDialogBinding = DialogBaseDoneBinding.inflate(inflater, container, false)
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
            textViewTitle.text = title
            setDoneClickListener()
        }
    }

    override fun getTheme(): Int = R.style.WalletTheme_BottomSheet_Rounded

    abstract fun getResult(): Any?

    private fun DialogBaseDoneBinding.setDoneClickListener() {
        buttonDone.setOnClickListener {
            setFragmentResult(requestKey, bundleOf(resultKey to getResult()))
            dismissAllowingStateLoss()
        }
    }
}
