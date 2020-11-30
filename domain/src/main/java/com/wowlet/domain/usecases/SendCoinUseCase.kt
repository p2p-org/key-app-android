package com.wowlet.domain.usecases

import com.wowlet.data.datastore.WowletApiCallRepository
import com.wowlet.domain.interactors.SendCoinInteractor
import com.wowlet.entities.local.SendTransactionModel
import com.wowlet.entities.local.WalletItem


class SendCoinUseCase(private val wowletApiCallRepository: WowletApiCallRepository) :
    SendCoinInteractor {

    override suspend fun sendCoin(coinData: SendTransactionModel): String {
       return wowletApiCallRepository.sendTransaction(coinData)
    }

}