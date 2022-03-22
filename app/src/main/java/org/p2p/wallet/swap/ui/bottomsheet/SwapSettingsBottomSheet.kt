package org.p2p.wallet.swap.ui.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.p2p.wallet.R
import org.p2p.wallet.databinding.DialogSwapSettingsBinding
import org.p2p.wallet.swap.model.Slippage
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

class SwapSettingsBottomSheet(
    private val onSlippageSelected: (Slippage) -> Unit
) : BottomSheetDialogFragment() {

    companion object {
        private const val EXTRA_SLIPPAGE = "EXTRA_SLIPPAGE"
        fun show(fm: FragmentManager, slippage: Slippage, onSlippageSelected: (Slippage) -> Unit) {
            SwapSettingsBottomSheet(onSlippageSelected)
                .withArgs(
                    EXTRA_SLIPPAGE to slippage,
                )
                .show(fm, SwapSlippageBottomSheet::javaClass.name)
        }
    }

    private val binding: DialogSwapSettingsBinding by viewBinding()

    private val slippage: Slippage by args(EXTRA_SLIPPAGE)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.dialog_swap_settings, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            slippageView.setBottomText(slippage.percentValue)
            slippageView.setOnClickListener { openSlippage() }
            payView.isVisible = false
            payView.setOnClickListener {
                Toast.makeText(requireContext(), "Not implemented yet", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openSlippage() {
        SwapSlippageBottomSheet.show(parentFragmentManager, slippage) {
            onSlippageSelected(it)
            binding.slippageView.setBottomText(slippage.percentValue)
        }
    }

    override fun getTheme(): Int = R.style.WalletTheme_BottomSheet_Rounded
}
