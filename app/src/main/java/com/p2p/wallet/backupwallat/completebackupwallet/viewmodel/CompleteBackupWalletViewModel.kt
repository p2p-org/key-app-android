package com.p2p.wallet.backupwallat.completebackupwallet.viewmodel

import com.p2p.wallet.deprecated.viewcommand.Command
import com.p2p.wallet.deprecated.viewmodel.BaseViewModel
import com.p2p.wallet.backupwallat.interactor.CompleteBackupWalletInteractor

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