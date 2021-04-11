package com.p2p.wowlet.auth.interactor

import com.p2p.wowlet.infrastructure.persistence.PreferenceService
import com.p2p.wowlet.common.network.CallException
import com.p2p.wowlet.common.network.Constants.Companion.PREFERENCE_SAVED_ERROR
import com.p2p.wowlet.common.network.Result
import com.p2p.wowlet.dashboard.model.local.PinCodeData

class EnterPinCodeInteractor(private val preferenceService: PreferenceService) {

    suspend fun initPinCode(pinCode: String): Result<Boolean> {

        return when (val data = preferenceService.setPinCodeValue(PinCodeData(pinCode.toInt()))) {
            true -> {
                Result.Success(data)
            }
            false -> {
                Result.Error(CallException(PREFERENCE_SAVED_ERROR, "Not saved pin code"))
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
                    PREFERENCE_SAVED_ERROR,
                    "In the storige not saved pin code"
                )
            )
        }
    }
}