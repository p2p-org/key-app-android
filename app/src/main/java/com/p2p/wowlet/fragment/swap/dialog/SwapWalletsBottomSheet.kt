package com.p2p.wowlet.fragment.swap.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.p2p.wowlet.R
import com.p2p.wowlet.databinding.DialogYourWalletBinding
import com.p2p.wowlet.fragment.swap.viewmodel.SwapViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_your_wallet.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class SwapWalletsBottomSheet : BottomSheetDialogFragment() {

    private val swapViewModel: SwapViewModel by viewModel()
    lateinit var binding: DialogYourWalletBinding

    companion object {
        fun newInstance(): SwapWalletsBottomSheet = SwapWalletsBottomSheet()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.dialog_your_wallet, container, false
        )
        binding.viewModel = swapViewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swapViewModel.getCoinList()

        vClose.setOnClickListener {
            dismiss()
        }
    }

    override fun onResume() {
        super.onResume()
        dialog?.run {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }
}