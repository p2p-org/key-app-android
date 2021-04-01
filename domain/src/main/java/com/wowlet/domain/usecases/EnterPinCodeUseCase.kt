package com.wowlet.domain.usecases

import com.wowlet.data.datastore.PreferenceService
import com.wowlet.domain.interactors.EnterPinCodeInteractor
import com.wowlet.entities.CallException
import com.wowlet.entities.Constants.Companion.PREFERENCE_SAVED_ERROR
import com.wowlet.entities.Result
import com.wowlet.entities.local.*

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
