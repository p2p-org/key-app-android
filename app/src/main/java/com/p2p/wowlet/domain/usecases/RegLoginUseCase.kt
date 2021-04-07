package com.p2p.wowlet.domain.usecases

import com.p2p.wowlet.datastore.PreferenceService
import com.p2p.wowlet.datastore.WowletApiCallRepository
import com.p2p.wowlet.domain.interactors.RegLoginInteractor

class RegLoginUseCase(
    private val preferenceService: PreferenceService,
    private val wowletApiCallRepository: WowletApiCallRepository
) : RegLoginInteractor {

}