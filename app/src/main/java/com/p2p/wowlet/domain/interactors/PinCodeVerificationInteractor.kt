package com.p2p.wowlet.domain.interactors

import com.p2p.wowlet.entities.Result

interface PinCodeVerificationInteractor {
  suspend fun verifyPinCode(pinCode: String): Result<Boolean>

}