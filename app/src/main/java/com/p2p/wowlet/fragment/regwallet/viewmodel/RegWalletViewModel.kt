package com.p2p.wowlet.fragment.regwallet.viewmodel

import com.p2p.wowlet.R
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.appbase.viewmodel.BaseViewModel

class RegWalletViewModel :BaseViewModel(){
    fun navigateUp() {
        _command.value=
            Command.NavigateUpViewCommand(R.id.action_navigation_reg_wallet_to_navigation_reg_login)
    }
    fun goToPinCodeFragment(){
        _command.value = Command.NavigatePinCodeViewCommand(R.id.action_navigation_reg_wallet_to_navigation_pin_code)
    }
}