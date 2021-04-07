package com.p2p.wowlet.domain.usecases

import com.p2p.wowlet.datastore.PreferenceService
import com.p2p.wowlet.domain.interactors.EnterPinCodeInteractor
import com.p2p.wowlet.entities.CallException
import com.p2p.wowlet.entities.Constants.Companion.PREFERENCE_SAVED_ERROR
import com.p2p.wowlet.entities.Result
import com.p2p.wowlet.entities.local.PinCodeData

class EnterPinCodeUseCase(private val preferenceService: PreferenceService) : EnterPinCodeInteractor {

    override suspend fun initPinCode(pinCode: String): Result<Boolean> {

       return when (val data = preferenceService.setPinCodeValue(PinCodeData(pinCode.toInt()))) {
            true -> {
                Result.Success(data)
            }
            false -> {
                Result.Error(CallException(PREFERENCE_SAVED_ERROR, "Not saved pin code"))
            }
        }
    }

    override suspend fun verifyPinCode(pinCode: String): Result<Boolean> {
        preferenceService.getPinCodeValue()?.let {
            return if (it.pinCode == pinCode.toInt()) {
                Result.Success(true)
            } else {
                Result.Success(false)
            }
        } ?: run {
            return Result.Error(
                CallException(
                    PREFERENCE_SAVED_ERROR,
                    "In the storige not saved pin code"
                )
            )
        }
    }
}
