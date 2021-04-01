package com.p2p.wowlet.fragment.dashboard.dialog.recoveryphrase

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.p2p.wowlet.R
import com.p2p.wowlet.databinding.DialogRecoveryPhraseBinding
import com.p2p.wowlet.fragment.dashboard.dialog.recoveryphrase.viewmodel.RecoveryPhraseViewModel
import com.p2p.wowlet.utils.copyClipboard
import kotlinx.android.synthetic.main.dialog_recovery_phrase.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class RecoveryPhraseDialog : DialogFragment() {
    private val viewModel: RecoveryPhraseViewModel by viewModel()
    private var phraseList = mutableListOf<String>()

    companion object {

        const val TAG_RECOVERY_DIALOG = "RecoveryPhraseDialog"
        fun newInstance(): RecoveryPhraseDialog {
            return RecoveryPhraseDialog()
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: DialogRecoveryPhraseBinding = DataBindingUtil.inflate(
            inflater, R.layout.dialog_recovery_phrase, container, false
        )
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vDone.setOnClickListener {
            dismiss()
        }
        vClose.setOnClickListener {
            dismiss()
        }
        lContainer.setOnClickListener {
            if (phraseList.isNotEmpty()) {
                val phraseStr = phraseList.joinToString(separator = " ")
                it.context.copyClipboard(phraseStr)
            }
        }
        observe()
    }

    private fun observe() {
        viewModel.dismissDialog.observe(viewLifecycleOwner, {
            dismiss()
        })
        viewModel.getSortSecretData.observe(viewLifecycleOwner, {
            phraseList.clear()
            phraseList.addAll(it)
        })
    }

    override fun onResume() {
        super.onResume()
        dialog?.run {
            val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
            window?.setLayout(width, ConstraintLayout.LayoutParams.WRAP_CONTENT)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            isCancelable = false
        }
    }
}