package com.p2p.wowlet.fragment.sendcoins.view

import android.os.Bundle
import android.view.View
import com.p2p.wowlet.fragment.sendcoins.dialog.SendCoinDoneDialog
import com.p2p.wowlet.fragment.sendcoins.dialog.YourWalletsBottomSheet
import com.p2p.wowlet.R
import com.p2p.wowlet.activity.MainActivity
import com.p2p.wowlet.appbase.FragmentBaseMVVM
import com.p2p.wowlet.appbase.utils.dataBinding
import com.p2p.wowlet.appbase.viewcommand.Command.*
import com.p2p.wowlet.appbase.viewcommand.ViewCommand
import com.p2p.wowlet.databinding.FragmentSendCoinsBinding
import com.p2p.wowlet.fragment.sendcoins.viewmodel.SendCoinsViewModel
import com.wowlet.entities.local.UserWalletType
import org.koin.androidx.viewmodel.ext.android.viewModel

class SendCoinsFragment : FragmentBaseMVVM<SendCoinsViewModel, FragmentSendCoinsBinding>() {

    override val viewModel: SendCoinsViewModel by viewModel()
    override val binding: FragmentSendCoinsBinding by dataBinding(R.layout.fragment_send_coins)

    companion object {
        const val WALLET_ADDRESS = "walletAddress"
    }

    private var walletAddress: String = ""
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {
            viewModel = this@SendCoinsFragment.viewModel
        }
        viewModel.initData( mutableListOf(
            UserWalletType(
                "Wallet address",
                walletAddress,
                false,
                R.drawable.ic_qr_scaner
            ), UserWalletType("Wallet user", "@username", true, R.drawable.ic_account)
        ))
    }

    override fun initData() {
        arguments?.let {
            walletAddress = it.getString(WALLET_ADDRESS, "")
        }
    }

    override fun processViewCommand(command: ViewCommand) {
        when (command) {
            is NavigateUpViewCommand -> navigateFragment(command.destinationId)
            is NavigateScannerViewCommand -> {
                navigateFragment(command.destinationId)
                (activity as MainActivity).showHideNav(false)
            }
            is OpenMyWalletDialogViewCommand -> {
                val yourWalletsBottomSheet: YourWalletsBottomSheet =
                    YourWalletsBottomSheet.newInstance()
                yourWalletsBottomSheet.show(childFragmentManager, YourWalletsBottomSheet.YOUR_WALLET)
            }
            is SendCoinDoneViewCommand -> {
                val sendCoinDoneDialog: SendCoinDoneDialog = SendCoinDoneDialog.newInstance {
                    navigateUp()
                }
                sendCoinDoneDialog.show(childFragmentManager, SendCoinDoneDialog.SEND_COIN_DONE)
            }
        }
    }

    override fun navigateUp() {
        viewModel.navigateUp()
    }
}