package com.p2p.wowlet.fragment.swap.view

import android.os.Bundle
import android.view.View
import com.p2p.wowlet.R
import com.p2p.wowlet.appbase.FragmentBaseMVVM
import com.p2p.wowlet.appbase.utils.dataBinding
import com.p2p.wowlet.appbase.viewcommand.Command.*
import com.p2p.wowlet.appbase.viewcommand.ViewCommand
import com.p2p.wowlet.databinding.FragmentSwapBinding
import com.p2p.wowlet.fragment.sendcoins.dialog.SendCoinDoneDialog
import com.p2p.wowlet.fragment.swap.dialog.SwapCoinProcessingDialog
import com.p2p.wowlet.fragment.swap.dialog.SwapWalletsBottomSheet
import com.p2p.wowlet.fragment.swap.viewmodel.SwapViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SwapFragment : FragmentBaseMVVM<SwapViewModel, FragmentSwapBinding>() {

    override val viewModel: SwapViewModel by viewModel()
    override val binding: FragmentSwapBinding by dataBinding(R.layout.fragment_swap)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {
            viewModel = this@SwapFragment.viewModel
        }
    }

    override fun processViewCommand(command: ViewCommand) {
        when (command) {
            is NavigateUpViewCommand -> navigateFragment(command.destinationId)
            is MyWalletDialogViewCommand -> {
                val swapWalletsBottomSheet: SwapWalletsBottomSheet =
                    SwapWalletsBottomSheet.newInstance()
                swapWalletsBottomSheet.show(childFragmentManager, "swap_wallet_bottom_fragment")
            }
            is SwapCoinProcessingViewCommand -> {
                val swapCoinProcessingDialog: SwapCoinProcessingDialog =
                    SwapCoinProcessingDialog.newInstance()
                swapCoinProcessingDialog.show(childFragmentManager, "swap_coin_processing_fragment")
            }
            is SendCoinDoneViewCommand -> {
                val yourWalletsBottomSheet: SendCoinDoneDialog = SendCoinDoneDialog.newInstance()
                yourWalletsBottomSheet.show(childFragmentManager, "send_coin_done_fragment")
            }
        }
    }
}