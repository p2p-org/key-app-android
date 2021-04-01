package com.wowlet.domain.usecases

import com.wowlet.data.datastore.PreferenceService
import com.wowlet.domain.interactors.FingerPrintInteractor
import com.wowlet.entities.local.EnableFingerPrintModel

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