package com.wowlet.domain.interactors

import com.wowlet.entities.local.EnableFingerPrintModel
import com.wowlet.entities.local.SecretKeyCombinationSuccess
import com.wowlet.entities.local.SecretKeyItem


interface FingerPrintInteractor {
    suspend fun setFingerPrint(isSetFingerPint: EnableFingerPrintModel)
     fun isEnableFingerPrint():EnableFingerPrintModel
}