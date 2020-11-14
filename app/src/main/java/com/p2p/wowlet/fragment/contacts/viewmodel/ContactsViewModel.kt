package com.p2p.wowlet.fragment.contacts.viewmodel
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.appbase.viewmodel.BaseViewModel

class ContactsViewModel :BaseViewModel(){
    fun finishApp() {
        _command.value =Command.FinishAppViewCommand()
    }
}