package com.p2p.wowlet.domain.usecases

import com.p2p.wowlet.datastore.PreferenceService

class CompleteBackupWalletInteractor(private val preferenceService: PreferenceService) {

    fun finishLoginReg(regFinish: Boolean) {
        preferenceService.finishLoginReg(regFinish)
    }
}