package com.wowlet.domain.interactors

import com.wowlet.entities.local.SendTransactionModel
import com.wowlet.entities.local.WalletItem

interface SendCoinInteractor {
  suspend  fun sendCoin(coinData:SendTransactionModel): String

}