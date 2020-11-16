package com.wowlet.domain.usecases

import com.wowlet.data.datastore.PreferenceService
import com.wowlet.domain.interactors.PinCodeInteractor
import com.wowlet.domain.interactors.SecretKeyInteractor
import com.wowlet.entities.local.PinCodeData
import com.wowlet.entities.local.SecretKeyCombinationSuccess
import com.wowlet.entities.local.SecretKeyItem


class PinCodeUseCase(private val preferenceService: PreferenceService) : PinCodeInteractor {

    override suspend fun initPinCode(pinCode: String): Boolean {
        val value = preferenceService.getPinCodeValue()
        return value?.let {
            it.pinCode == pinCode.toInt()
        } ?: kotlin.run {
            preferenceService.setPinCodeValue(PinCodeData(pinCode.toInt()))
            true
        }
    }
}