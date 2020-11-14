package com.p2p.wowlet.fragment.backupwallat.recoverywallat.viewmodel

import com.p2p.wowlet.R
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.appbase.viewmodel.BaseViewModel

class RecoveryWalletViewModel ():BaseViewModel(){
    fun goToSecretKeyFragment() {
        _command.value=
            Command.NavigateSecretKeyViewCommand(R.id.action_navigation_recovery_wallet_to_navigation_secret_key)
    }
    fun navigateUp() {
        _command.value=
            Command.NavigateUpViewCommand(R.id.action_navigation_recovery_wallet_to_navigation_reg_login)
    }
}