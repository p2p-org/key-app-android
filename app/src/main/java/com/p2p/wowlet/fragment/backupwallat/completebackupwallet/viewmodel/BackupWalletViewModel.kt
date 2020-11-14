package com.p2p.wowlet.fragment.backupwallat.completebackupwallet.viewmodel

import com.p2p.wowlet.R
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.appbase.viewmodel.BaseViewModel

class BackupWalletViewModel :BaseViewModel(){
    fun navigateUp() {
        _command.value=
            Command.NavigateUpViewCommand(R.id.action_navigation_complete_wallet_to_navigation_secret_key)
    }
    fun openMainActivityViewCommand() {
        _command.value =
            Command.OpenMainActivityViewCommand()
    }
}