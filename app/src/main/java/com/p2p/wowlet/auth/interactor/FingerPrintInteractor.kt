package com.p2p.wowlet.auth.interactor

import com.p2p.wowlet.infrastructure.persistence.PreferenceService
import com.p2p.wowlet.dashboard.model.local.EnableFingerPrintModel

class FingerPrintInteractor(private val preferenceService: PreferenceService) {

    suspend fun setFingerPrint(isSetFingerPint: EnableFingerPrintModel) {
        preferenceService.enableFingerPrint(isSetFingerPint)
    }

    fun isEnableFingerPrint(): EnableFingerPrintModel {
        val data = preferenceService.isSetFingerPrint()
        return data ?: EnableFingerPrintModel(false, false)
    }
}