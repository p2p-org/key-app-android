package com.wowlet.domain.usecases

import com.wowlet.data.datastore.PreferenceService
import com.wowlet.data.datastore.WowletApiCallRepository
import com.wowlet.domain.interactors.SendCoinInteractor
import com.wowlet.entities.CallException
import com.wowlet.entities.Constants.Companion.REQUEST_EXACTION
import com.wowlet.entities.local.SendTransactionModel
import java.lang.Exception
import com.wowlet.entities.Result

class SendCoinUseCase(
    private val wowletApiCallRepository: WowletApiCallRepository,
    private val preferenceService: PreferenceService
) : SendCoinInteractor {

    override suspend fun sendCoin(coinData: SendTransactionModel): Result<String> {
        val activeWallet = preferenceService.getActiveWallet()
        coinData.secretKey = activeWallet?.secretKey!!
        coinData.fromPublicKey = activeWallet?.publicKey!!
        try {
            return Result.Success(wowletApiCallRepository.sendTransaction(coinData))
        } catch (e: Exception) {
            return Result.Error(CallException(REQUEST_EXACTION,e.message))
        }

    }

}