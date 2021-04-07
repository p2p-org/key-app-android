package com.p2p.wowlet.fragment.regfinish.viewmodel

import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.appbase.viewmodel.BaseViewModel
import com.p2p.wowlet.domain.interactors.RegFinishInteractor

class RegFinishViewModel(val regFinishInteractor: RegFinishInteractor) : BaseViewModel() {

    init {
        checkFinishReg()
    }

    fun openMainActivityViewCommand() {
        finishRegistration()
        _command.value =
            Command.OpenMainActivityViewCommand()
    }

    private fun finishRegistration() {
        regFinishInteractor.finishLoginReg(true)
    }

    private fun checkFinishReg() {
        val data = regFinishInteractor.isCurrentLoginReg()
        if (data) {
            openMainActivityViewCommand()
        }
    }
}