package com.wowlet.domain.interactors

import com.wowlet.entities.local.SendTransactionModel

interface SendCoinInteractor {
  suspend  fun sendCoin(coinData:SendTransactionModel): String
}