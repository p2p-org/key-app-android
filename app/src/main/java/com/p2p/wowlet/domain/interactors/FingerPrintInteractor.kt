package com.p2p.wowlet.domain.interactors

import com.p2p.wowlet.entities.local.EnableFingerPrintModel

interface FingerPrintInteractor {
    suspend fun setFingerPrint(isSetFingerPint: EnableFingerPrintModel)
     fun isEnableFingerPrint(): EnableFingerPrintModel
}