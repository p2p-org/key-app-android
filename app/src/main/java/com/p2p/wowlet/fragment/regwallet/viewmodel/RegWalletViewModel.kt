package com.p2p.wowlet.fragment.regwallet.viewmodel

import androidx.core.os.bundleOf
import com.p2p.wowlet.R
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.appbase.viewmodel.BaseViewModel
import com.p2p.wowlet.fragment.pincode.view.PinCodeFragment
import com.wowlet.entities.enums.PinCodeFragmentType

class RegWalletViewModel :BaseViewModel(){
    fun navigateUp() {
        _command.value=
            Command.NavigateUpViewCommand(R.id.action_navigation_reg_wallet_to_navigation_reg_login)
    }
    fun goToPinCodeFragment(){
        _command.value = Command.NavigatePinCodeViewCommand(R.id.action_navigation_reg_wallet_to_navigation_pin_code,
            bundleOf(
                PinCodeFragment.OPEN_FRAGMENT_SPLASH_SCREEN to false,
                PinCodeFragment.CREATE_NEW_PIN_CODE to  PinCodeFragmentType.CREATE))
    }
}