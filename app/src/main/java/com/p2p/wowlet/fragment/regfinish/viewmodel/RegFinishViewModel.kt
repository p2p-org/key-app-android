package com.p2p.wowlet.fragment.regfinish.viewmodel

import com.p2p.wowlet.R
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.appbase.viewmodel.BaseViewModel
import com.wowlet.domain.interactors.RegFinishInteractor

class RegFinishViewModel(val regFinishInteractor: RegFinishInteractor) :BaseViewModel(){

    fun navigateUp() {
        _command.value= Command.NavigateUpViewCommand(R.id.action_navigation_reg_finish_to_navigation_notification)
    }
    fun openMainActivityViewCommand() {
        finishRegistration()
        _command.value =
            Command.OpenMainActivityViewCommand()
    }
   private fun finishRegistration(){
        regFinishInteractor.finishReg(true)
    }

}