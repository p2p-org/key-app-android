package com.p2p.wowlet.fragment.qrscanner.viewmodel

import androidx.core.os.bundleOf
import com.p2p.wowlet.R
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.appbase.viewmodel.BaseViewModel
import com.p2p.wowlet.fragment.sendcoins.view.SendCoinsFragment.Companion.WALLET_ADDRESS

class QrScannerViewModel : BaseViewModel() {


    fun navigateUp() {
        _command.value =
            Command.NavigateUpViewCommand(R.id.action_navigation_scanner_to_navigation_dashboard)
    }

    fun goToSendCoinFragment(walletAddress:String) {
        _command.value =
            Command.NavigateSendCoinViewCommand(R.id.action_navigation_scanner_to_navigation_send_coin,
                bundleOf(WALLET_ADDRESS to walletAddress))
    }
}