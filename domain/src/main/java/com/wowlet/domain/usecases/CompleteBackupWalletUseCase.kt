package com.wowlet.domain.usecases

import com.wowlet.data.datastore.PreferenceService
import com.wowlet.domain.interactors.CompleteBackupWalletInteractor



class CompleteBackupWalletUseCase(private val preferenceService: PreferenceService) : CompleteBackupWalletInteractor {

    override fun finishLoginReg(regFinish: Boolean) {
        preferenceService.finishLoginReg(regFinish)
    }
}