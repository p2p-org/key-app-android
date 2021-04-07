package com.p2p.wowlet.domain.interactors

import com.p2p.wowlet.entities.Result

interface PinCodeInteractor {
    suspend fun initPinCode(pinCode: String): Result<Boolean>
    suspend fun verifyPinCode(pinCode: String): Result<Boolean>
}