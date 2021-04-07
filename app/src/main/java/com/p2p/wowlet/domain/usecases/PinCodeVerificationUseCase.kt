package com.p2p.wowlet.domain.usecases
import com.p2p.wowlet.datastore.PreferenceService
import com.p2p.wowlet.domain.interactors.PinCodeVerificationInteractor
import com.p2p.wowlet.entities.CallException
import com.p2p.wowlet.entities.Constants
import com.p2p.wowlet.entities.Result

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