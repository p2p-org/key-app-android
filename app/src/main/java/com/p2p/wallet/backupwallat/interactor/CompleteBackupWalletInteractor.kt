package com.p2p.wallet.backupwallat.interactor

import com.p2p.wallet.infrastructure.persistence.PreferenceService

class CompleteBackupWalletInteractor(private val preferenceService: PreferenceService) {

    fun finishLoginReg(regFinish: Boolean) {
        preferenceService.finishLoginReg(regFinish)
    }
}