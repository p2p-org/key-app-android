package com.wowlet.domain.usecases

import com.wowlet.data.datastore.PreferenceService
import com.wowlet.data.datastore.WowletApiCallRepository
import com.wowlet.domain.extentions.transferInfoToActivityItem
import com.wowlet.domain.interactors.SendCoinInteractor
import com.wowlet.entities.CallException
import com.wowlet.entities.Constants.Companion.REQUEST_EXACTION
import com.wowlet.entities.local.SendTransactionModel
import java.lang.Exception
import com.wowlet.entities.Result
import com.wowlet.entities.local.ActivityItem

class SendCoinUseCase(
    private val wowletApiCallRepository: WowletApiCallRepository,
    private val preferenceService: PreferenceService
) : SendCoinInteractor {

    override suspend fun sendCoin(coinData: SendTransactionModel): Result<ActivityItem> {
        val activeWallet = preferenceService.getActiveWallet()
        coinData.secretKey = activeWallet?.secretKey!!
        coinData.fromPublicKey = activeWallet?.publicKey!!
        try {
            val signature = wowletApiCallRepository.sendTransaction(coinData)
            val transaction = wowletApiCallRepository.getConfirmedTransaction(
                signature,
                0.0
            )?.transferInfoToActivityItem(coinData.fromPublicKey, "", "")

            return Result.Success(transaction)
        } catch (e: Exception) {
            return Result.Error(CallException(REQUEST_EXACTION, e.message))
        }
    }
}