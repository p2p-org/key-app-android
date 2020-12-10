package com.wowlet.domain.interactors

import com.wowlet.entities.Result
import com.wowlet.entities.local.EnableFingerPrintModel
import com.wowlet.entities.local.EnableNotificationModel
import com.wowlet.entities.local.PinCodeVerification

interface QrScannerInteractor {
    suspend fun getAccountInfo(publicKey: String): Result<Boolean>
}