package com.p2p.wallet.token.repository

import com.p2p.wallet.main.api.CompareApi
import com.p2p.wallet.token.model.PriceHistory

class TokenRemoteRepository(
    private val compareApi: CompareApi,
) : TokenRepository {

    companion object {
        private const val CHART_ENTRY_LIMIT = 20
    }

    override suspend fun getDailyPriceHistory(sourceToken: String, destination: String, days: Int): List<PriceHistory> {
        val response = compareApi.getDailyPriceHistory(sourceToken, destination, days)

        return if (response.message.isNullOrEmpty()) {
            response.data.list.map { PriceHistory(it.close) }
        } else emptyList()
    }

    override suspend fun getHourlyPriceHistory(
        sourceToken: String,
        destination: String,
        hours: Int
    ): List<PriceHistory> {
        val response = compareApi.getHourlyPriceHistory(sourceToken, destination, hours, CHART_ENTRY_LIMIT)
        return if (response.message.isNullOrEmpty()) {
            response.data.list.map { PriceHistory(it.close) }
        } else emptyList()
    }
}