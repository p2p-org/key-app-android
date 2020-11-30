package com.p2p.wowlet.fragment.termandcondition.viewmodel

import com.p2p.wowlet.R
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.appbase.viewmodel.BaseViewModel

class TermAndConditionViewModel:BaseViewModel() {

    fun goToCreateWalletFragment() {
        _command.value =
            Command.NavigateCreateWalletViewCommand(R.id.action_navigation_term_and_condition_to_navigation_create_wallet)
    }

    fun navigateUp() {
        _command.value =
            Command.NavigateUpViewCommand(R.id.action_navigation_term_and_condition_to_navigation_reg_login)
    }
}