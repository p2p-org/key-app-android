package com.wowlet.domain.interactors

import com.wowlet.entities.Result
import com.wowlet.entities.local.EnableFingerPrintModel
import com.wowlet.entities.local.EnableNotificationModel
import com.wowlet.entities.local.PinCodeVerification

interface PinCodeInteractor {
    suspend fun initPinCode(pinCode: String): Result<Boolean>
    suspend fun verifyPinCode(pinCode: String): Result<Boolean>
}