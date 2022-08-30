package org.p2p.wallet.common.ui.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    private val resultKey: String by args(ARG_RESULT_KEY)
    private val requestKey: String by args(ARG_REQUEST_KEY)

    lateinit var binding: DialogBaseDoneBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogBaseDoneBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
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
