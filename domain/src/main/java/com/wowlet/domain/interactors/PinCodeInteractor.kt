package com.wowlet.domain.interactors

import com.wowlet.entities.local.SecretKeyCombinationSuccess
import com.wowlet.entities.local.SecretKeyItem

interface PinCodeInteractor {
  suspend  fun initPinCode(pinCode:String): Boolean
}