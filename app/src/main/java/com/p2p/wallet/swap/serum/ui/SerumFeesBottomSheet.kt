package com.p2p.wallet.swap.serum.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.p2p.wallet.R
import com.p2p.wallet.databinding.DialogSwapFeesBottomSheetBinding
import com.p2p.wallet.utils.args
import com.p2p.wallet.utils.viewbinding.viewBinding
import com.p2p.wallet.utils.withArgs

class SerumFeesBottomSheet : BottomSheetDialogFragment() {

    companion object {
        private const val EXTRA_LIQUIDITY_FEE = "EXTRA_LIQUIDITY_FEE"
        private const val EXTRA_NETWORK_FEE = "EXTRA_NETWORK_FEE"
        private const val EXTRA_FEE_OPTION = "EXTRA_FEE_OPTION"
        fun show(
            fm: FragmentManager,
            liquidityFee: String,
            networkFee: String,
            alternativeFeeOptions: String
        ) {
            SerumFeesBottomSheet()
                .withArgs(
                    EXTRA_LIQUIDITY_FEE to liquidityFee,
                    EXTRA_NETWORK_FEE to networkFee,
                    EXTRA_FEE_OPTION to alternativeFeeOptions
                )
                .show(fm, SerumSlippageBottomSheet::javaClass.name)
        }
    }

    private val binding: DialogSwapFeesBottomSheetBinding by viewBinding()

    private val liquidityFee: String by args(EXTRA_LIQUIDITY_FEE)
    private val networkFee: String by args(EXTRA_NETWORK_FEE)
    private val alternativeFeeOptions: String by args(EXTRA_FEE_OPTION)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.dialog_swap_fees_bottom_sheet, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            liquidityFeeView.setBottomText(liquidityFee)
            networkFeeView.setBottomText(networkFee)
            payView.setBottomText(alternativeFeeOptions)
            payView.setOnClickListener {
                Toast.makeText(requireContext(), "Not implemented yet", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getTheme(): Int = R.style.WalletTheme_BottomSheet_Rounded
}