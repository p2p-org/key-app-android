package com.p2p.wowlet.fragment.regfinish.viewmodel

import com.p2p.wowlet.R
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.appbase.viewmodel.BaseViewModel

class RegFinishViewModel :BaseViewModel(){

    fun navigateUp() {
        _command.value= Command.NavigateUpViewCommand(R.id.action_navigation_reg_finish_to_navigation_notification)
    }
    fun openMainActivityViewCommand() {
        _command.value =
            Command.OpenMainActivityViewCommand()
    }


}