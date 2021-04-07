package com.p2p.wowlet.domain.usecases

import com.p2p.wowlet.datastore.PreferenceService
import com.p2p.wowlet.domain.interactors.RegFinishInteractor


class RegFinishUseCase(private val preferenceService: PreferenceService) : RegFinishInteractor {
    override fun finishLoginReg(regFinish: Boolean) {
        preferenceService.finishLoginReg(regFinish)
    }

    override fun isCurrentLoginReg(): Boolean = preferenceService.isCurrentLoginReg()

}