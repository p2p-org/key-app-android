package com.p2p.wowlet.fragment.dashboard.dialog.swap.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.p2p.wowlet.R
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.appbase.viewcommand.ViewCommand
import com.p2p.wowlet.databinding.BottomSheetSlippageSettingsBinding
import com.p2p.wowlet.fragment.backupwallat.secretkeys.utils.hideSoftKeyboard
import com.p2p.wowlet.fragment.backupwallat.secretkeys.utils.showSoftKeyboard
import com.p2p.wowlet.fragment.dashboard.dialog.swap.viewmodel.SwapViewModel
import com.p2p.wowlet.supportclass.widget.CheckableButton
import com.p2p.wowlet.supportclass.widget.CheckableButtonGroup
import com.p2p.wowlet.utils.toast
import com.p2p.wowlet.utils.viewbinding.viewBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class SlippageSettingsBottomSheet(
    private val selectSlippage: (slippage: Double) -> Unit
) : BottomSheetDialogFragment() {

    private val swapViewModel: SwapViewModel by viewModel()

    private val binding: BottomSheetSlippageSettingsBinding by viewBinding()

    private var slippage: Double = 0.1

    companion object {
        const val TAG_SLIPPAGE_SETTINGS = "SlippageSettingsBottomSheet"
        fun newInstance(
            selectSlippage: (slippage: Double) -> Unit
        ): SlippageSettingsBottomSheet {
            return SlippageSettingsBottomSheet(selectSlippage)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.bottom_sheet_slippage_settings, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {
            lSlippageCheckableBG.addClickEvents(
                selectSlippageClickEvent,
                HashMap<Int, CheckableButtonGroup.OnClickEvent>().apply {
                    put(5, customSlippageClickEVent)
                })

            editorView.setOnClickListener { swapViewModel.setFocusOnSlippageEditor() }
            editorTextView.isVisible = swapViewModel.isSlippageEditorEmpty.value == true
            closeImageView.setOnClickListener { swapViewModel.clearSlippageEditor() }
        }
        observeData()
    }

    private fun observeData() {
        swapViewModel.command.observe(viewLifecycleOwner) { initViewCommands(it) }
        swapViewModel.selectedSlippage.observe(viewLifecycleOwner) {
            if (swapViewModel.isCustomSlippageEditorVisible.value == true
                && swapViewModel.isSlippageEditorEmpty.value == true
            ) {
                toast("Please select a slippage")
                return@observe
            }
            selectSlippage.invoke(slippage)
            dismiss()
        }
        swapViewModel.isFocusOnCustomSlippageEditor.observe(viewLifecycleOwner) {
            binding.edtCustomSlippage.apply {
                requestFocus()
                showSoftKeyboard()
            }
        }
        swapViewModel.clearSlippageEditor.observe(viewLifecycleOwner) {
            binding.edtCustomSlippage.setText("")
        }
    }

    private fun initViewCommands(command: ViewCommand) {
        when (command) {
            is Command.NavigateUpBackStackCommand -> {
                dismiss()
            }
        }
    }

    private val selectSlippageClickEvent: (v: CheckableButton) -> Unit = {
        slippage = it.text.removeSuffix("%").toString().toDouble()
        swapViewModel.makeCustomSlippageEditorVisible(false)
        binding.edtCustomSlippage.hideSoftKeyboard()
    }

    private val customSlippageClickEVent: (v: CheckableButton) -> Unit = {
        val slippageText = binding.edtCustomSlippage.text.toString()
        slippage = if (slippageText == "." || slippageText == "") 0.0 else slippageText.toDouble()
        binding.edtCustomSlippage.doAfterTextChanged {
            swapViewModel.setIsSlippageEditorEmpty(it.toString().isEmpty())
            val plainText: String = it.toString()
            slippage = if (plainText == "." || plainText == "") 0.0 else plainText.toDouble()
        }
        swapViewModel.makeCustomSlippageEditorVisible(true)
        swapViewModel.setFocusOnSlippageEditor()
    }
}