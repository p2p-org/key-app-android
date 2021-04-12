package com.p2p.wallet.auth.interactor

import com.p2p.wallet.infrastructure.persistence.PreferenceService
import com.p2p.wallet.common.network.CallException
import com.p2p.wallet.common.network.Constants
import com.p2p.wallet.common.network.Result
import com.p2p.wallet.dashboard.model.local.PinCodeResponse

class PinCodeInteractor(private val preferenceService: PreferenceService) {
    suspend fun initPinCode(pinCode: String): Result<Boolean> {
        return when (val data = preferenceService.setPinCodeValue(PinCodeResponse(pinCode.toInt()))) {
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