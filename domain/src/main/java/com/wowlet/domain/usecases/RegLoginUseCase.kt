package com.wowlet.domain.usecases

import com.wowlet.data.datastore.PreferenceService
import com.wowlet.data.datastore.WowletApiCallRepository
import com.wowlet.domain.interactors.RegLoginInteractor
import com.wowlet.entities.local.UserSecretData

class RegLoginUseCase(
    private val preferenceService: PreferenceService,
    private val wowletApiCallRepository: WowletApiCallRepository
) : RegLoginInteractor {

}