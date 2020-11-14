package com.p2p.wowlet.fragment.detailinsight.viewmodel
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.appbase.viewmodel.BaseViewModel

class InsightViewModel : BaseViewModel(){
    fun finishApp() {
        _command.value = Command.FinishAppViewCommand()
    }
}