package com.p2p.wowlet.fragment.reglogin.viewmodel
import com.p2p.wowlet.R
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.appbase.viewmodel.BaseViewModel
import com.wowlet.domain.interactors.RegLoginInteractor

class RegLoginViewModel(val regLoginInteractor: RegLoginInteractor) : BaseViewModel() {


    fun finishApp() {
        _command.value = Command.FinishAppViewCommand()
    }

}