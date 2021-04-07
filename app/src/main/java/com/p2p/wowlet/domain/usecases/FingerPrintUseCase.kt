package com.p2p.wowlet.domain.usecases

import com.p2p.wowlet.datastore.PreferenceService
import com.p2p.wowlet.domain.interactors.FingerPrintInteractor
import com.p2p.wowlet.entities.local.EnableFingerPrintModel

class FingerPrintUseCase(private val preferenceService: PreferenceService) :
    FingerPrintInteractor {

    override suspend fun setFingerPrint(isSetFingerPint: EnableFingerPrintModel) {
            preferenceService.enableFingerPrint(isSetFingerPint)
    }

    override  fun isEnableFingerPrint(): EnableFingerPrintModel {
        val data = preferenceService.isSetFingerPrint()
        return data ?: EnableFingerPrintModel(false, false)
    }

}