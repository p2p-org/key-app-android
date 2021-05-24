package com.p2p.wallet.swap.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.p2p.wallet.R
import com.p2p.wallet.databinding.DialogSlippageBottomSheetBinding
import com.p2p.wallet.swap.model.Slippage
import com.p2p.wallet.utils.args
import com.p2p.wallet.utils.viewbinding.viewBinding
import com.p2p.wallet.utils.withArgs

class SlippageBottomSheet(
    private val onSlippageSelected: (Double) -> Unit
) : BottomSheetDialogFragment() {

    companion object {
        private const val EXTRA_SLIPPAGE = "EXTRA_SLIPPAGE"
        fun show(fm: FragmentManager, currentSlippage: Slippage, onSlippageSelected: (Double) -> Unit) {
            SlippageBottomSheet(onSlippageSelected)
                .withArgs(EXTRA_SLIPPAGE to currentSlippage)
                .show(fm, SlippageBottomSheet::javaClass.name)
        }
    }

    private val binding: DialogSlippageBottomSheetBinding by viewBinding()

    private val initialSlippage: Slippage by args(EXTRA_SLIPPAGE)

    private var slippage: Double = 0.0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.dialog_slippage_bottom_sheet, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            slippageRadioView.setCurrentSlippage(initialSlippage)

            slippageRadioView.onSlippageChanged = {
                slippage = it
            }

            doneButton.setOnClickListener {
                onSlippageSelected(slippage)
                dismissAllowingStateLoss()
            }
        }
    }

    override fun getTheme(): Int = R.style.WalletTheme_BottomSheet_Rounded
}