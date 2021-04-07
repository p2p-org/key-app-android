package com.p2p.wowlet.fragment.backupwallat.completebackupwallet.viewmodel

import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.appbase.viewmodel.BaseViewModel
import com.p2p.wowlet.domain.interactors.CompleteBackupWalletInteractor

class CompleteBackupWalletViewModel(val completeBackupWalletInteractor: CompleteBackupWalletInteractor) :
    BaseViewModel() {
    fun openMainActivityViewCommand() {
        finishRegistration()
        _command.value =
            Command.OpenMainActivityViewCommand()
    }

    fun finishRegistration() {
        completeBackupWalletInteractor.finishLoginReg(true)
    }
}