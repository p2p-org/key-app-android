package com.p2p.wowlet.auth.interactor

import com.p2p.wowlet.datastore.PreferenceService
import com.p2p.wowlet.entities.CallException
import com.p2p.wowlet.entities.Constants
import com.p2p.wowlet.entities.Result
import com.p2p.wowlet.entities.local.PinCodeData

class PinCodeInteractor(private val preferenceService: PreferenceService) {
    suspend fun initPinCode(pinCode: String): Result<Boolean> {
        return when (val data = preferenceService.setPinCodeValue(PinCodeData(pinCode.toInt()))) {
            true -> {
                Result.Success(data)
            }
            false -> {
                Result.Error(CallException(Constants.PREFERENCE_SAVED_ERROR, "Not saved pin code"))
            }
        }
    }

    suspend fun verifyPinCode(pinCode: String): Result<Boolean> {
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