package com.p2p.wowlet.fragment.investments.viewmodel
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.appbase.viewmodel.BaseViewModel


class InvestmentsViewModel : BaseViewModel(){
    fun finishApp() {
        _command.value = Command.FinishAppViewCommand()
    }
}