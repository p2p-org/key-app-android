package com.wowlet.domain.interactors

import com.wowlet.entities.Result
import com.wowlet.entities.local.PinCodeVerification


interface EnterPinCodeInteractor {
    suspend fun initPinCode(pinCode: String):  Result<Boolean>
    suspend fun verifyPinCode(pinCode: String): Result<Boolean>


}