package com.wowlet.domain.usecases
import com.wowlet.data.datastore.PreferenceService
import com.wowlet.domain.interactors.PinCodeInteractor
import com.wowlet.domain.interactors.PinCodeVerificationInteractor
import com.wowlet.entities.CallException
import com.wowlet.entities.Constants
import com.wowlet.entities.Result
import com.wowlet.entities.local.PinCodeData

class PinCodeUseCase(private val preferenceService: PreferenceService) :
    PinCodeInteractor {
    override suspend fun initPinCode(pinCode: String): Result<Boolean> {
        return when (val data = preferenceService.setPinCodeValue(PinCodeData(pinCode.toInt()))) {
            true -> {
                Result.Success(data)
            }
            false -> {
                Result.Error(CallException(Constants.PREFERENCE_SAVED_ERROR, "Not saved pin code"))
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
                    Constants.PREFERENCE_SAVED_ERROR,
                    "In the storage not saved pin code"
                )
            )
        }
    }


}