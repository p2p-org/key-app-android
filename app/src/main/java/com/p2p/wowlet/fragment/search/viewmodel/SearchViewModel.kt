package com.p2p.wowlet.fragment.search.viewmodel
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.appbase.viewmodel.BaseViewModel

class SearchViewModel :BaseViewModel(){
    fun finishApp() {
        _command.value =Command.FinishAppViewCommand()
    }
}