package com.wowlet.domain.interactors

import com.wowlet.entities.local.SendTransactionModel
import com.wowlet.entities.Result

interface SendCoinInteractor {
  suspend  fun sendCoin(coinData:SendTransactionModel): Result<String>

}