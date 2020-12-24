package com.wowlet.domain.interactors

import com.wowlet.entities.local.SendTransactionModel
import com.wowlet.entities.Result
import com.wowlet.entities.local.ActivityItem
import java.math.BigDecimal

interface SendCoinInteractor {
    suspend fun sendCoin(coinData: SendTransactionModel): Result<ActivityItem>

    suspend fun getFee(): Result<BigDecimal>

}