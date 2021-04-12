package com.p2p.wallet.auth.interactor

import com.p2p.wallet.infrastructure.persistence.PreferenceService

class RegFinishInteractor(private val preferenceService: PreferenceService) {
    fun finishLoginReg(regFinish: Boolean) {
        preferenceService.finishLoginReg(regFinish)
    }

    fun isCurrentLoginReg(): Boolean = preferenceService.isCurrentLoginReg()
}