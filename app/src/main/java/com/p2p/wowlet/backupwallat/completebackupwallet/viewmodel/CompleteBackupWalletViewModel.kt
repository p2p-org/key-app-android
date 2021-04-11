package com.p2p.wowlet.backupwallat.completebackupwallet.viewmodel

import com.p2p.wowlet.deprecated.viewcommand.Command
import com.p2p.wowlet.deprecated.viewmodel.BaseViewModel
import com.p2p.wowlet.domain.usecases.CompleteBackupWalletInteractor

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