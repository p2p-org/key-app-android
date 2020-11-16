package com.wowlet.domain.usecases

import com.wowlet.data.datastore.PreferenceService
import com.wowlet.domain.interactors.RegFinishInteractor


class RegFinishUseCase(private val preferenceService: PreferenceService) : RegFinishInteractor {
    override fun finishReg(regFinish: Boolean) {
        preferenceService.finishReg(regFinish)
    }
}