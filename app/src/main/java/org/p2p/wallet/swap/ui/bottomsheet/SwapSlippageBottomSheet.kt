package org.p2p.wallet.swap.ui.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.p2p.wallet.R
import org.p2p.wallet.databinding.DialogSlippageBottomSheetBinding
import org.p2p.wallet.swap.model.Slippage
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

class SwapSlippageBottomSheet(
    private val onSlippageSelected: (Slippage) -> Unit
) : BottomSheetDialogFragment() {

    companion object {
        private const val EXTRA_SLIPPAGE = "EXTRA_SLIPPAGE"
        fun show(fm: FragmentManager, currentSlippage: Slippage, onSlippageSelected: (Slippage) -> Unit) {
            SwapSlippageBottomSheet(onSlippageSelected)
                .withArgs(EXTRA_SLIPPAGE to currentSlippage)
                .show(fm, SwapSlippageBottomSheet::javaClass.name)
        }
    }

    private val binding: DialogSlippageBottomSheetBinding by viewBinding()

    private val initialSlippage: Slippage by args(EXTRA_SLIPPAGE)

    private var slippage: Slippage = Slippage.Min

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.dialog_slippage_bottom_sheet, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            backImageView.setOnClickListener { dismissAllowingStateLoss() }
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
