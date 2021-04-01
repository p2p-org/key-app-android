package com.wowlet.domain.usecases
import com.wowlet.data.datastore.PreferenceService
import com.wowlet.domain.interactors.PinCodeVerificationInteractor
import com.wowlet.entities.CallException
import com.wowlet.entities.Constants
import com.wowlet.entities.Result

class PinCodeVerificationUseCase(private val preferenceService: PreferenceService) :
    PinCodeVerificationInteractor {

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