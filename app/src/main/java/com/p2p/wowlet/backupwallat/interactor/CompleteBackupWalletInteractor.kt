package com.p2p.wowlet.backupwallat.interactor

import com.p2p.wowlet.infrastructure.persistence.PreferenceService

class CompleteBackupWalletInteractor(private val preferenceService: PreferenceService) {

    fun finishLoginReg(regFinish: Boolean) {
        preferenceService.finishLoginReg(regFinish)
    }
}