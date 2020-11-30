package com.wowlet.domain.usecases

import com.wowlet.data.datastore.PreferenceService
import com.wowlet.domain.interactors.SplashScreenInteractor


class SplashScreenUseCase(private val preferenceService: PreferenceService) :
    SplashScreenInteractor {

    override fun isCurrentLoginReg(): Boolean {
        return preferenceService.isCurrentLoginReg()
    }

}