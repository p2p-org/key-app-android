package com.p2p.wowlet.domain.usecases

import com.p2p.wowlet.datastore.PreferenceService
import com.p2p.wowlet.domain.interactors.CompleteBackupWalletInteractor



class CompleteBackupWalletUseCase(private val preferenceService: PreferenceService) : CompleteBackupWalletInteractor {

    override fun finishLoginReg(regFinish: Boolean) {
        preferenceService.finishLoginReg(regFinish)
    }
}